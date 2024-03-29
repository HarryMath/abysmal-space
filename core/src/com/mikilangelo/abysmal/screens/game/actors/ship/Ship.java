package com.mikilangelo.abysmal.screens.game.actors.ship;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.PlayerState;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.SimplifiedState;
import com.mikilangelo.abysmal.shared.Settings;
import com.mikilangelo.abysmal.shared.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.shared.repositories.LasersRepository;
import com.mikilangelo.abysmal.shared.repositories.ParticlesRepository;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.shared.defenitions.EngineDef;
import com.mikilangelo.abysmal.shared.defenitions.ShipDef;
import com.mikilangelo.abysmal.shared.defenitions.TurretDef;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.EngineAnimation;
import com.mikilangelo.abysmal.screens.game.objectsData.ShieldData;
import com.mikilangelo.abysmal.screens.game.objectsData.ShipData;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.screens.game.GameScreen;

public class Ship {

  private static short totalShips = 0;
  public final short bodyId;

  public ShipDef def;
  public String generationId;
  public Body body;
  public Body shield;
  public ShieldData shieldData;
  public ShipData bodyData;
  protected Body primaryBody;
  protected Body secondaryBody;
  public Array<Turret> turrets;
  public float newAngle = MathUtils.PI / 2;
  private final EngineAnimation engineAnimation;

  public float x;
  public float y;
  public float angle = MathUtils.PI / 2;
  public Vector2 velocity;
  public float speed;
  public float currentPower = 0;
  public float distance;

  private long lastShotTime = 0;
  private long lastShieldOnTime = 0;
  private float shieldTimeLeft = 0;
  public boolean isPowerApplied = false;
  protected boolean isUnderControl = false;
  public boolean shieldOn = false;
  public int ammo;

  protected float speedCoefficient = 1;
  private long lastSpeedUp = 0;
  private float speedTimeLeft = 0;

  protected final float controlSpeedResistance;
  protected final float simpleSpeedResistance;
  private final float shieldSize;
  private float shieldScale;
  private final Sprite shieldTexture = new Sprite(TexturesRepository.get("things/shield.png"));
  private final Sprite shieldTouchTexture = new Sprite(TexturesRepository.get("things/shieldTouch.png"));
  private final Array<ShieldTouch> shieldTouches = new Array<>();

  public Ship(ShipDef definition, float x, float y, boolean isPlayer, float playerX, float playerY) {
    totalShips++;
    bodyId = (short)(-totalShips);
    this.generationId = CalculateUtils.uid();
    this.def = definition;
    engineAnimation = new EngineAnimation(definition.engineAnimation, definition.frameFrequency);
    this.turrets = new Array<>();
    for (short i = 0; i < definition.turretDefinitions.size; i++) {
      TurretDef t = definition.turretDefinitions.get(i);
      final Turret turret = t.isAutomatic && isPlayer ?
              new AutomaticTurret(t, generationId) :
              new Turret(t, generationId, i);
      this.turrets.add(turret);
    }
    def.resizeTextures(1);
    this.controlSpeedResistance = (0.9983f - this.def.speedResistance / 2);
    this.simpleSpeedResistance = 0.99955f - this.def.speedResistance * this.def.speedResistance;
    this.x = x; this.y = y;
    this.distance = isPlayer ? 0 : CalculateUtils.distance(playerX, playerY, x, y);
    // shieldRadius = 0.63f * definition.size * (float) Math.hypot(1, definition.bodyTexture.getWidth() / definition.bodyTexture.getHeight());
    shieldSize = this.def.shieldRadius * 2 / shieldTexture.getHeight();
    shieldScale = shieldSize;
    ammo = this.def.ammo;
  }

  // fields to draw in debug mode
  public float aimX = 0;
  public float aimY = 0;
  public float normalX = 0;
  public float normalY = 0;

  public void control(float direction, float power, float delta) {
    newAngle = direction;
    final float rotationLeft = (angle - newAngle + MathUtils.PI2) % MathUtils.PI2;
    final float rotationRight = (newAngle - angle + MathUtils.PI2) % MathUtils.PI2;
    final boolean isLeft = rotationLeft < rotationRight;
    float rotation = isLeft ? rotationLeft : rotationRight;
    final float powerScale = (3.1415927f - rotation) / 3.14159267f;
    this.applyImpulse(power * (0.3f + powerScale * 0.7f), this.distance < SCREEN_WIDTH);

    assert angle >= 0 && angle <= MathUtils.PI2;
    assert newAngle >= 0 && newAngle <= MathUtils.PI2;

    // rotation = rotation > 1f ? 1 : (rotation / 1f * 0.7f + 0.3f);
    rotation = rotation > 1f ? 1 : (rotation * 0.7f + 0.3f);
    if (angle != newAngle) {
      this.rotate(isLeft ? -rotation : rotation, delta);
      if (angle <= newAngle + def.controlPower * 0.15f && angle >= newAngle - def.controlPower * 0.15f) {
        this.body.setAngularVelocity(this.body.getAngularVelocity() * 0.9f);
      }
    }
  }

  // power: [0, 1]
  public void applyImpulse(float power, boolean withParticles) {
    if (power > 0.85f) {
      power = power * speedCoefficient;
    }
    this.currentPower = (this.currentPower * 0.99f + power * 0.01f);
    isPowerApplied = isUnderControl = true;
    primaryBody.applyLinearImpulse(
            power * MathUtils.cos(angle) * def.speedPower * 0.0135f,
            power * MathUtils.sin(angle) * def.speedPower * 0.0135f,
            primaryBody.getPosition().x,
            primaryBody.getPosition().y,
            true);
    velocity = primaryBody.getLinearVelocity();
    primaryBody.setLinearVelocity(
            velocity.x * controlSpeedResistance,
            velocity.y * controlSpeedResistance);
    if (withParticles) kak();
  }

  public void handleStop() {
    applyImpulse(-0.01f, false);
  }

  public void handleRotate(float p, float delta) {
    rotate(p, delta);
  }

  protected void rotate(float direction, float delta) {
    isUnderControl = true;
    this.body.setAngularVelocity(this.body.getAngularVelocity() + direction * def.controlPower / (0.99f + delta));
    if (!isPowerApplied) {
      primaryBody.setLinearVelocity(
              velocity.x * controlSpeedResistance,
              velocity.y * controlSpeedResistance);
    }
  }

  public void setState(PlayerState state, float delta) {
    if (state.shieldOn && !shieldOn) {
      activateShield();
    } else if (!state.shieldOn && shieldOn) {
      stopShield();
    }
    if (delta < 0) { delta = 0; }
    float speedM = 0.1f + 0.9f / (1 + delta * 0.5f);
    x = state.x + state.speedX * delta * speedM;
    y = state.y + state.speedY * delta * speedM;
    angle = CalculateUtils.normalizeAngle(state.angle + state.angularSpeed * delta * 0.91f);
    primaryBody.setTransform(x, y, angle);
    secondaryBody.setTransform(x, y, angle);
    speedM = 0.3f + speedM * 0.7f;
    primaryBody.setLinearVelocity(
            state.speedX * speedM,
            state.speedY * speedM
    );
    primaryBody.setAngularVelocity(state.angularSpeed * speedM);
  }

  public void setSimpleState(SimplifiedState state) {
    x = state.x;
    y = state.y;
    primaryBody.setTransform(x, y, angle);
    secondaryBody.setTransform(x, y, angle);
    primaryBody.setAngularVelocity(primaryBody.getAngularVelocity() * 0.95f);
    primaryBody.setLinearVelocity(velocity.x * 0.95f, velocity.y * 0.95f);
  }

  public void setState(PlayerState state, float delta, float c1) {
    x = primaryBody.getPosition().x;
    y = primaryBody.getPosition().y;
    velocity = primaryBody.getLinearVelocity();
    float c2 = 1 - c1;
    angle = primaryBody.getAngle();
    if (state.shieldOn && !shieldOn) {
      activateShield();
    } else if (!state.shieldOn && shieldOn) {
      stopShield();
    }
    if (delta < 0) { delta = 0; }
    float speedM = 0.1f + 0.9f / (1 + delta * 0.5f);
    x = c2 * x + c1 * (state.x + state.speedX * delta * speedM);
    y = c2 * y + c1 * (state.y + state.speedY * delta * speedM);
    primaryBody.setTransform(x, y, angle);
    secondaryBody.setTransform(x, y, angle);
    speedM = 0.3f + speedM * 0.7f;
    primaryBody.setAngularVelocity(
            primaryBody.getAngularVelocity() * c2 +
                    c1 * state.angularSpeed * speedM
    );
    c1 = c1 * 0.65f + 0.35f;
    c2 = 1 - c1;
    primaryBody.setLinearVelocity(
            velocity.x * c2 + c1 * state.speedX * speedM,
            velocity.y * c2 + c1 * state.speedY * speedM
    );
    angle = CalculateUtils.avgAngle(
            angle, c2,
            state.angle + state.angularSpeed * delta * 0.95f, c1
    );

  }

  public void move(float delta, float playerX, float playerY) {
    move(delta);
    this.distance = CalculateUtils.distance(playerX, playerY, x, y);
  }

  public void move(float delta) {
    x = primaryBody.getPosition().x;
    y = primaryBody.getPosition().y;
    velocity = primaryBody.getLinearVelocity();
    angle = CalculateUtils.normalizeAngle(this.body.getAngle());
    this.body.setAngularVelocity(this.body.getAngularVelocity() * def.rotationResistance);
    primaryBody.setLinearVelocity(velocity.scl(simpleSpeedResistance));
    // primaryBody.setTransform(x, y, angle);
    secondaryBody.setTransform(x, y, angle);
    secondaryBody.setLinearVelocity(velocity);
    speed = (float) Math.hypot(velocity.x, velocity.y);
    for (Turret t : turrets) {
      t.move(angle, x, y, delta);
    }
    if (isUnderControl) {
      this.body.setAngularVelocity(this.body.getAngularVelocity() * def.rotationControlResistance);
      isUnderControl = false;
    } else {
      this.currentPower *= 0.97f;
    }
    if (speedCoefficient > 1) {
      speedTimeLeft -= delta;
      if (speedTimeLeft <= 0) {
        speedCoefficient = 1;
      }
    }
    if (bodyData.health < 15) {
      if (MathUtils.random() < 0.03f) {
        ParticlesRepository.addSmoke(new ParticleSmog(x, y, velocity.x * 0.3f, velocity.y * 0.3f));
      }
      if (MathUtils.random() < (15 - bodyData.health) / 60) {
        ParticlesRepository.addSmoke(new ParticleSmog(x, y, velocity.x * 0.7f, velocity.y * 0.7f));
        ParticlesRepository.addFire(new ParticleFire(x, y, velocity.x, velocity.y));
      }
      if (bodyData.health < 10) {
        if (MathUtils.random() < (15 - bodyData.health) / 15) {
          ParticlesRepository.addFire(new ParticleFire(x, y, velocity.x, velocity.y));
          bodyData.health -= 0.001f;
        } else if (MathUtils.random() < (10 - bodyData.health) / 40) {
          ParticlesRepository.addSmoke(new ParticleSmog(x, y, velocity.x, velocity.y));
        }
      }
    }
  }

  public void playShotSound(int gunId) {
    if (gunId < 0) {
      this.def.laserDefinition.sound.play(
              (140 - distance) / 141,
              1,
              (x - PlayerShip.X) / distance
      );
    } else {
      this.turrets.get(gunId).playShotSound(
              (140 - distance) / 141,
              (x - PlayerShip.X) / distance
      );
    }
  }

  public float getShotReloadTime(long currentTime) {
    return (def.shotIntervalMs + lastShotTime - currentTime) / 1000f;
  }

  public float getSpeedReloadTime(long currentTime) {
    return (def.speedRechargeTimeMs + lastSpeedUp - currentTime) / 1000f;
  }

  protected void shotDirectly(float soundScale, float pan) {
    if (def.lasersAmount < 1) return;
    final long newShotTime = System.currentTimeMillis();
    if (def.lasersAmount > ammo || (newShotTime - lastShotTime) < def.shotIntervalMs) {
      return;
    }
    final float maxLeftLaser = -def.lasersDistance * def.lasersAmount / 2f + def.lasersDistance / 2f;
    final float shipBiasX = def.laserX * MathUtils.cos(angle);
    final float shipBiasY = def.laserX * MathUtils.sin(angle);
    for (byte i = 0; i < def.lasersAmount; i++) {
      final float addCos = (maxLeftLaser + def.lasersDistance * i) * MathUtils.cos(angle + 1.5708f);
      final float addSin = (maxLeftLaser + def.lasersDistance * i) * MathUtils.sin(angle + 1.5708f);
      Laser l = new Laser(
              def.laserDefinition,
              x + addCos + shipBiasX,
              y + addSin + shipBiasY,
              this.angle,
              velocity.x,
              velocity.y,
              generationId,
              bodyId);
      if (this instanceof PlayerShip) {
        Laser.lastShotData.gunId = -1;
        Laser.lastShotData.withSound = i == 0;
        GameScreen.enemiesProcessor.shot(Laser.lastShotData);
      }
      LasersRepository.addSimple(l);
    }
    def.laserDefinition.sound.play(soundScale, 1, pan);
    this.ammo -= def.lasersAmount;
    this.lastShotTime = newShotTime;
  }

  public void shotDirectly() {
    if (distance >= 140) {
      this.shotDirectly(0, 0);
    } else {
      this.shotDirectly((140 - distance) / 141, (x - PlayerShip.X) / distance);
    }
  }

  public void shotByTurrets() {
    if (turrets.size == 0) return;
    final float soundScale = distance < 140 ? (140 - distance) / 141: 0;
    final float pan = distance < 140 ? (x - PlayerShip.X) / distance: 0;
    for (byte i = 0; i < turrets.size; i++) {
      turrets.get(i).shot(this, soundScale, pan);
    }
  }

  public void draw(Batch batch, float delta) {
    x = primaryBody.getPosition().x;
    y = primaryBody.getPosition().y;
    if (shieldOn) {
      shieldTexture.setScale(shieldScale);
      shieldTexture.setCenter(x, y);
      shieldTexture.setAlpha(0.15f);
      shieldTexture.setRotation(30);
      shieldTexture.draw(batch);
    }
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
    if (def.decorUnder != null) {
      def.decorUnder.setCenter(x, y);
      def.decorUnder.setRotation(this.angle * MathUtils.radiansToDegrees);
      def.decorUnder.setAlpha(
              def.decorOnSpeed ? Math.max(Math.min(this.currentPower, 1), 0) : 1
      );
      def.decorUnder.draw(batch);
    }
    engineAnimation.draw(batch, delta, x, y, angle);
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    def.bodyTexture.setRotation(this.angle * MathUtils.radiansToDegrees);
    def.bodyTexture.setCenter(x, y);
    def.bodyTexture.draw(batch);
    if (def.decorOver != null) {
      if (def.decorOver.isLight) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
      }
      def.decorOver.texture.setCenter(x, y);
      def.decorOver.texture.setRotation(this.angle * MathUtils.radiansToDegrees);
      def.decorOver.texture.setAlpha(Math.min(speed / def.maxSpeed, 1));
      def.decorOver.texture.draw(batch);
      if (def.decorOver.isLight) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      }
    }
    for (int i = 0; i < turrets.size; i++) {
      turrets.get(i).draw(batch, angle);
    }
    processShield(batch, delta);
  }

  private void processShield(Batch batch, float delta) {
    if (shieldOn) {
      shieldTimeLeft -= delta;
      if (shieldTimeLeft <= 0) {
        stopShield();
        return;
      }
      float touchPower = 0.05f;
      for (ShieldData.Touch touch: shieldData.lastTouches) {
        if (touch.power > touchPower) {
          touchPower = touch.power;
          shieldTouches.add(new ShieldTouch(touch.angle));
          if (shieldTouches.size > 10) {
            shieldTouches.removeIndex(0);
          }
        }
      }
      if (shieldData.lastTouches.size > 0) {
        shieldData.lastTouches.clear();
        if (touchPower > 0.05f && shieldTouches.size < 8) {
          ExplosionsRepository.shieldHid(distance, touchPower);
        }
      }
      shieldTouchTexture.setCenter(x, y);
      shieldTouchTexture.setScale(shieldScale);
      shieldTexture.setAlpha(0.55f);
      if (shieldTimeLeft < 3) {
        shieldTexture.setAlpha(0.55f * shieldTimeLeft * 0.333f);
      } else if (def.shieldLifeTimeS - shieldTimeLeft < 0.5f) {
        shieldScale = shieldSize * (def.shieldLifeTimeS - shieldTimeLeft) * 2;
      } else {
        shieldScale = shieldSize;
      }
      shieldTexture.draw(batch);
      for (byte i = 0; i < shieldTouches.size; i++) {
        if (shieldTouches.get(i).move()) {
          shieldTouchTexture.setAlpha(shieldTouches.get(i).opacity);
          shieldTouchTexture.setRotation(shieldTouches.get(i).angle);
          shieldTouchTexture.draw(batch);
        } else {
          shieldTouches.removeIndex(i--);
        }
      }
    }
  }

  public void kak() {
    if ( Settings.withParticles) {
      for (EngineDef e : def.engineDefinitions) {
        if (e.particleAppearChance < 1 && e.particleAppearChance < MathUtils.random()) {
          continue;
        }
        float screwX = e.positionY * MathUtils.cos(this.angle + MathUtils.PI / 2);
        float screwY = e.positionY * MathUtils.sin(this.angle + MathUtils.PI / 2);
        float posX = x + e.positionX * MathUtils.cos(this.angle);
        float posY = y + e.positionX * MathUtils.sin(this.angle);
        ParticlesRepository.add(
                e.isResizing ? new ResizingParticle(
                        e, posX + screwX, posY + screwY,
                        velocity.x, velocity.y) :
                new Particle(
                e, posX + screwX, posY + screwY,
                velocity.x, velocity.y), e.isTopLayer);
      }
    }
  }

  public void createBody(World world) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.x = x; bodyDef.position.y = y;
    this.body = world.createBody(bodyDef);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = def.density;
    fixtureDef.friction = def.friction;
    fixtureDef.restitution = def.restitution;
    fixtureDef.filter.groupIndex = bodyId;
    def.bodyLoader.attachFixture(this.body, "Name", fixtureDef, def.bodyScale);
    body.setTransform(x, y, angle);
    ShipData data = new ShipData();
    data.id = generationId;
    data.health = def.health;
    bodyData = data;
    this.body.setUserData(data);
    primaryBody = body;
    velocity = primaryBody.getLinearVelocity();
    createShield(world);
    if (def.maxSpeed == 0) {
      final float resistance = controlSpeedResistance * simpleSpeedResistance;
      def.maxSpeed = 0.01352f * def.speedPower * resistance /
              body.getMass() / (1 - resistance);
    }
  }

  private void createShield(World world) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.x = x; bodyDef.position.y = y;
    CircleShape shape = new CircleShape();
    shape.setRadius(def.shieldRadius);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = body.getMass() / (def.shieldRadius * def.shieldRadius * MathUtils.PI);
    fixtureDef.friction = def.friction * 0.14f;
    fixtureDef.restitution = 0.3f * 0.95f + def.restitution * 0.05f;
    fixtureDef.shape = shape;
    fixtureDef.filter.groupIndex = bodyId;
    shield = world.createBody(bodyDef);
    shield.createFixture(fixtureDef);
    shape.dispose();
    shieldData = new ShieldData();
    shieldData.shipId = generationId;
    shield.setUserData(shieldData);
    shield.setFixedRotation(true);
    shield.setActive(false);
    shield.setAwake(false);
    shieldOn = false;
    secondaryBody = shield;
  }

  public float getShieldAbilityReloadTime(long currentTime) {
    return (def.shieldRechargeTimeMs + lastShieldOnTime - currentTime) / 1000f;
  }

  public void activateShield() {
    activateShield(false);
  }

  public void speedUp() {
    final long currentTime = System.currentTimeMillis();
    if (currentTime - lastSpeedUp > def.speedRechargeTimeMs) {
      this.speedCoefficient = def.speedUpCoefficient;
      speedTimeLeft = def.speedTimeS;
      lastSpeedUp = currentTime;
    }
  }

  public void activateShield(boolean force) {
    final long currentTime = System.currentTimeMillis();
    if (force || currentTime - lastShieldOnTime > def.shieldRechargeTimeMs) {
      shieldOn = true;
      primaryBody = shield;
      secondaryBody = body;
      shield.setActive(true);
      shield.setAwake(true);
      lastShieldOnTime = currentTime;
      shieldTimeLeft = def.shieldLifeTimeS;
      shieldScale = shieldSize * 0.1f;
      velocity = primaryBody.getLinearVelocity();
    }
  }

  private void stopShield() {
    shieldOn = false;
    primaryBody = body;
    secondaryBody = shield;
    velocity = primaryBody.getLinearVelocity();
    shield.setActive(false);
    shield.setAwake(false);
  }

  public void destroy(World world) {
    world.destroyBody(this.body);
    world.destroyBody(this.shield);
    turrets.clear();
    shieldTouches.clear();
    this.body = null;
    this.shield = null;
  }

  private static class ShieldTouch {

    private final float angle;
    public float opacity;

    private ShieldTouch(float angle) {
      this.opacity = 1;
      this.angle = angle;
    }

    public boolean move() {
      this.opacity -= 0.025f;
      return opacity > 0;
    }
  }

}
