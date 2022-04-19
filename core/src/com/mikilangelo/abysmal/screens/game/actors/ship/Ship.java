package com.mikilangelo.abysmal.screens.game.actors.ship;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
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
import com.badlogic.gdx.utils.TimeUtils;
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
  private Body primaryBody;
  private Body secondaryBody;
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
  private boolean isUnderControl = false;
  public boolean shieldOn = false;
  public int ammo;

  private final float controlSpeedResistance;
  private final float simpleSpeedResistance;
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

  public void control(float direction, float power, float delta) {
    newAngle = direction;
    final float rotationLeft = (angle - newAngle + MathUtils.PI2) % MathUtils.PI2;
    final float rotationRight = (newAngle - angle + MathUtils.PI2) % MathUtils.PI2;
    final boolean isLeft = rotationLeft < rotationRight;
    final float rotation = isLeft ? rotationLeft : rotationRight;
    final float powerScale = (3.1415927f - rotation) / 3.14159267f;
    this.applyImpulse(power * (0.1f + powerScale * 0.9f), this.distance < SCREEN_WIDTH);

    assert angle >= 0 && angle <= MathUtils.PI2;
    assert newAngle >= 0 && newAngle <= MathUtils.PI2;

    if (angle != newAngle) {
      if (angle <= newAngle + def.controlPower / 5f && angle >= newAngle - def.controlPower / 5f) {
        this.body.setAngularVelocity(this.body.getAngularVelocity() * 0.82f);
      } else {
        if (isLeft) {
          this.rotate(- rotationLeft / MathUtils.PI * 0.55f - 0.45f, delta);
        } else {
          this.rotate(rotationLeft / MathUtils.PI * 0.55f + 0.45f, delta);
        }
      }
    }
  }

  // power: [0, 1]
  public void applyImpulse(float power, boolean withParticles) {
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

  public void rotate(float direction, float delta) {
    isUnderControl = true;
    this.body.setAngularVelocity(this.body.getAngularVelocity() + direction * def.controlPower / (0.99f + delta));
    if (!isPowerApplied) {
      primaryBody.setLinearVelocity(
              velocity.x * controlSpeedResistance,
              velocity.y * controlSpeedResistance);
    }
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
    primaryBody.setTransform(x, y, angle);
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

  protected void shotDirectly(float soundScale, float pan) {
    if (def.lasersAmount < 1) return;
    final long newShotTime = TimeUtils.millis();
    if (def.lasersAmount > ammo || (newShotTime - lastShotTime) < def.shotInterval) {
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
//      def.decorUnder.setAlpha(Math.min(speed / def.maxSpeed, 1));
      def.decorUnder.setAlpha(Math.max(Math.min(this.currentPower, 1), 0));
      def.decorUnder.draw(batch);
    }
    engineAnimation.draw(batch, delta, x, y, angle);
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    def.bodyTexture.setRotation(this.angle * MathUtils.radiansToDegrees);
    def.bodyTexture.setCenter(x, y);
    def.bodyTexture.draw(batch);
    if (def.decorOver != null) {
      def.decorOver.setCenter(x, y);
      def.decorOver.setRotation(this.angle * MathUtils.radiansToDegrees);
      def.decorOver.setAlpha(Math.min(speed / def.maxSpeed, 1));
      def.decorOver.draw(batch);
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
      } else if (def.shieldLifeTime - shieldTimeLeft < 0.5f) {
        shieldScale = shieldSize * (def.shieldLifeTime - shieldTimeLeft) * 2;
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
                new EngineParticle(
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

  public void activateShield() {
    final long currentTime = TimeUtils.millis();
    if (currentTime - lastShieldOnTime > def.shieldRechargeTime) {
      shieldOn = true;
      primaryBody = shield;
      secondaryBody = body;
      shield.setActive(true);
      shield.setAwake(true);
      lastShieldOnTime = currentTime;
      shieldTimeLeft = def.shieldLifeTime;
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
