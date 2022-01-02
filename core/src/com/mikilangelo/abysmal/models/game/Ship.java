package com.mikilangelo.abysmal.models.game;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;

import com.mikilangelo.abysmal.components.Settings;
import com.mikilangelo.abysmal.components.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.components.repositories.LasersRepository;
import com.mikilangelo.abysmal.components.repositories.ParticlesRepository;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.models.definitions.EngineDef;
import com.mikilangelo.abysmal.models.definitions.ShipDef;
import com.mikilangelo.abysmal.models.definitions.TurretDef;
import com.mikilangelo.abysmal.models.game.animations.EngineAnimation;
import com.mikilangelo.abysmal.models.game.extended.AutomaticTurret;
import com.mikilangelo.abysmal.models.game.extended.EngineParticle;
import com.mikilangelo.abysmal.models.game.extended.Laser;
import com.mikilangelo.abysmal.models.game.extended.Turret;
import com.mikilangelo.abysmal.models.objectsData.ShieldData;
import com.mikilangelo.abysmal.models.objectsData.ShipData;
import com.mikilangelo.abysmal.tools.Geometry;
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
import com.mikilangelo.abysmal.ui.screens.GameScreen;

public class Ship {

  private static int totalShips = 0;

  public ShipDef definition;
  public String generationId;
  public Body body;
  public Body shield;
  public ShieldData shieldData;
  private Body primaryBody;
  private Body secondaryBody;
  public Array<Turret> turrets;
  public float angle = MathUtils.PI / 2;
  public float newAngle = MathUtils.PI / 2;
  EngineAnimation engineAnimation;
  public float x;
  public float y;
  public float distance;
  private long lastShotTime = 0;
  private long lastShieldOnTime = 0;
  private float shieldTimeLeft = 0;
  public boolean underControl = false;
  public boolean shieldOn = false;
  public int ammo;

  private final float controlSpeedResistance;
  private final float simpleSpeedResistance;
  private final float shieldSize;
  private float shieldScale;
  private final Sprite shieldTexture = new Sprite(TexturesRepository.get("things/shield.png"));
  private final Sprite shieldTouchTexture = new Sprite(TexturesRepository.get("things/shieldTouch.png"));
  private final Array<ShieldTouch> shieldTouches = new Array<>();

  public Ship(ShipDef def, float x, float y, boolean isPlayer, float playerX, float playerY) {
    totalShips++;
    this.generationId = def.name + (totalShips % 10) + MathUtils.random(0, 9) +
            String.valueOf(System.currentTimeMillis()).substring(5);
    this.definition = def;
    engineAnimation = new EngineAnimation(def.engineAnimation, def.frameFrequency);
    this.turrets = new Array<>();
    for (TurretDef t : def.turretDefinitions) {
      final Turret turret = t.isAutomatic && isPlayer ?
              new AutomaticTurret(t, generationId) :
              new Turret(t, generationId);
      this.turrets.add(turret);
    }
    this.definition.bodyTexture.setScale( definition.size / definition.bodyTexture.getHeight() );
    for (byte i = 0; i < definition.engineAnimation.size; i++) {
      definition.engineAnimation.get(i).setScale( definition.bodyTexture.getScaleY() );
    }
    this.controlSpeedResistance = (0.9983f - definition.speedResistance / 2);
    this.simpleSpeedResistance = 0.99955f - definition.speedResistance * definition.speedResistance;
    this.x = x; this.y = y;
    this.distance = isPlayer ? 0 : Geometry.distance(playerX, playerY, x, y);
    // shieldRadius = 0.63f * definition.size * (float) Math.hypot(1, definition.bodyTexture.getWidth() / definition.bodyTexture.getHeight());
    shieldSize = definition.shieldRadius * 2 / shieldTexture.getHeight();
    shieldScale = shieldSize;
    ammo = definition.ammo;
  }

  public void control(float direction, float power, float delta) {
    newAngle = direction;
    this.applyImpulse(power, this.distance < SCREEN_WIDTH);

    assert angle >= 0 && angle <= MathUtils.PI2;
    assert newAngle >= 0 && newAngle <= MathUtils.PI2;

    if (angle != newAngle) {
      if (angle <= newAngle + definition.controlPower / 5f && angle >= newAngle - definition.controlPower / 5f) {
        this.body.setAngularVelocity(this.body.getAngularVelocity() * 0.82f);
      } else {
        final float rotationLeft = (angle - newAngle + MathUtils.PI2) % MathUtils.PI2;
        final float rotationRight = (newAngle - angle + MathUtils.PI2) % MathUtils.PI2;
        if (rotationLeft < rotationRight) {
          this.rotate(- rotationLeft / MathUtils.PI * 0.55f - 0.45f, delta);
        } else {
          this.rotate(rotationLeft / MathUtils.PI * 0.55f + 0.45f, delta);
        }
      }
    }
  }

  // power: [0, 1]
  public void applyImpulse(float power, boolean withParticles) {
    primaryBody.applyLinearImpulse(
            power * MathUtils.cos(angle) * definition.speedPower * 0.0135f,
            power * MathUtils.sin(angle) * definition.speedPower * 0.0135f,
            primaryBody.getPosition().x,
            primaryBody.getPosition().y,
            true);
    primaryBody.setLinearVelocity(
            primaryBody.getLinearVelocity().x * controlSpeedResistance,
            primaryBody.getLinearVelocity().y * controlSpeedResistance);
    if (withParticles) kak();
  }

  public void rotate(float direction, float delta) {
    this.body.setAngularVelocity(this.body.getAngularVelocity() + direction * definition.controlPower / (0.99f + delta));
    if (!underControl) {
      primaryBody.setLinearVelocity(
              primaryBody.getLinearVelocity().x * controlSpeedResistance,
              primaryBody.getLinearVelocity().y * controlSpeedResistance);
    }
    this.body.setAngularVelocity(this.body.getAngularVelocity() * (0.999f - definition.rotationResistance));
  }

  public void move(float delta, float playerX, float playerY) {
    move(delta);
    this.distance = Geometry.distance(playerX, playerY, x, y);
  }

  public void move(float delta) {
    x = primaryBody.getPosition().x;
    y = primaryBody.getPosition().y;
    this.angle = Geometry.normalizeAngle(this.body.getAngle());
    this.body.setAngularVelocity(this.body.getAngularVelocity() * (1 - definition.rotationResistance));
    primaryBody.setLinearVelocity(primaryBody.getLinearVelocity().scl(simpleSpeedResistance));
    primaryBody.setTransform(x, y, angle);
    secondaryBody.setTransform(x, y, angle);
    secondaryBody.setLinearVelocity(primaryBody.getLinearVelocity());
    for (Turret t : turrets) {
      t.move(angle, x, y, delta);
    }
  }

  public void shotDirectly(float x, float y, float sX, float sY, long deltaMillis) {
    if (distance >= 140) {
      this.shotDirectly(0, 0, x, y, sX, sY, deltaMillis);
    } else {
      this.shotDirectly((140 - distance) / 141, (x - PlayerShip.X) / distance,
              x, y, sX, sY, deltaMillis);
    }
  }

  private void shotDirectly(float soundScale, float pan,
                           float x, float y, float sX, float sY, long deltaMillis
  ) {
    final long newShotTime = TimeUtils.millis() - deltaMillis;
    final float delay = deltaMillis * 0.001f;
    if (turrets.size > 0 || (newShotTime - lastShotTime) < definition.shotInterval) {
      return;
    }
    float maxLeftLaser = -definition.lasersDistance * definition.lasersAmount / 2f + definition.lasersDistance / 2f;
    for (byte i = 0; i < definition.lasersAmount; i++) {
      float addCos = (maxLeftLaser + definition.lasersDistance * i) * MathUtils.cos(this.angle + MathUtils.PI / 2);
      float addSin = (maxLeftLaser + definition.lasersDistance * i) * MathUtils.sin(this.angle + MathUtils.PI / 2);
      Laser l = new Laser(definition.laserDefinition,
              x + addCos, y + addSin, this.angle,
              sX, sY, generationId, delay);
      LasersRepository.addSimple(l);
    }
    definition.laserDefinition.sound.play(soundScale, 1, pan);
    this.ammo -= definition.lasersAmount;
    this.lastShotTime = newShotTime;
  }

  protected void shot(float soundScale, float pan) {
    if (ammo <= definition.lasersAmount || (definition.lasersAmount < 1 && definition.turretDefinitions.size == 0)) {
      return;
    }
    for (byte i = 0; i < turrets.size; i++) {
      turrets.get(i).shot(this, soundScale);
    }
    final long newShotTime = TimeUtils.millis();
    if (turrets.size > 0 || (newShotTime - lastShotTime) < definition.shotInterval) {
      return;
    }
    if (distance < 0.01f) {
      GameScreen.enemiesProcessor.shot();
    }
    shotDirectly(soundScale, pan,
            this.body.getPosition().x,
            this.body.getPosition().y,
            this.body.getLinearVelocity().x,
            this.body.getLinearVelocity().y,
            0);
  }

  public void shot() {
    if (distance >= 140) {
      this.shot(0, 0);
    } else {
      this.shot((140 - distance) / 141, (x - PlayerShip.X) / distance);
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
    engineAnimation.draw(batch, delta, x, y, angle);
    definition.bodyTexture.setRotation(this.angle * MathUtils.radiansToDegrees);
    definition.bodyTexture.setCenter(x, y);
    definition.bodyTexture.draw(batch);
    if (definition.decor != null) {
      definition.decor.setCenter(x, y);
      definition.decor.setRotation(this.angle * MathUtils.radiansToDegrees);
      definition.decor.setAlpha(Math.min(body.getLinearVelocity().len() / definition.maxSpeed, 1));
      definition.decor.draw(batch);
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
      for (float angle: shieldData.lastTouches) {
        shieldTouches.add(new ShieldTouch(angle));
        if (shieldTouches.size > 20) {
          shieldTouches.removeIndex(0);
        }
      }
      if (shieldData.lastTouches.size > 0) {
        shieldData.lastTouches.clear();
        ExplosionsRepository.shieldHid(distance);
      }
      shieldTouchTexture.setCenter(x, y);
      shieldTouchTexture.setScale(shieldScale);
      shieldTexture.setAlpha(0.65f);
      if (shieldTimeLeft < 3) {
        shieldTexture.setAlpha(0.65f * shieldTimeLeft / 3);
      } else if (definition.shieldLifeTime - shieldTimeLeft < 0.5f) {
        shieldScale = shieldSize * (definition.shieldLifeTime - shieldTimeLeft) * 2;
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
    underControl = true;
    if ( Settings.withParticles) {
      for (EngineDef e : definition.engineDefinitions) {
        float screwX = e.positionY * MathUtils.cos(this.angle + MathUtils.PI / 2);
        float screwY = e.positionY * MathUtils.sin(this.angle + MathUtils.PI / 2);
        float posX = x + e.positionX * MathUtils.cos(this.angle);
        float posY = y + e.positionX * MathUtils.sin(this.angle);
        ParticlesRepository.add(new EngineParticle(
                e, posX + screwX, posY + screwY,
                body.getLinearVelocity().x,
                body.getLinearVelocity().y
        ), e.isTopLayer);
      }
    }
  }

  public void createBody(World world) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.x = x; bodyDef.position.y = y;
    this.body = world.createBody(bodyDef);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = definition.density;
    fixtureDef.friction = definition.friction;
    fixtureDef.restitution = definition.restitution;
    definition.bodyLoader.attachFixture(this.body, "Name", fixtureDef, definition.bodyScale);
    body.setTransform(x, y, angle);
    ShipData data = new ShipData();
    data.id = generationId;
    data.health = definition.health;
    this.body.setUserData(data);
    primaryBody = body;
    createShield(world);
    if (definition.maxSpeed == 0) {
      final float resistance = controlSpeedResistance * simpleSpeedResistance;
      definition.maxSpeed = 0.01352f * definition.speedPower * resistance /
              body.getMass() / (1 - resistance);
    }
  }

  private void createShield(World world) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.x = x; bodyDef.position.y = y;
    CircleShape shape = new CircleShape();
    shape.setRadius(definition.shieldRadius);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = body.getMass() / (definition.shieldRadius * definition.shieldRadius * MathUtils.PI);
    fixtureDef.friction = definition.friction * 0.2f;
    fixtureDef.restitution = (1 + definition.restitution) * 0.5f;
    fixtureDef.shape = shape;
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
    if (currentTime - lastShieldOnTime > definition.shieldRechargeTime) {
      shieldOn = true;
      primaryBody = shield;
      secondaryBody = body;
      shield.setActive(true);
      shield.setAwake(true);
      lastShieldOnTime = currentTime;
      shieldTimeLeft = definition.shieldLifeTime;
    }
  }

  private void stopShield() {
    shieldOn = false;
    primaryBody = body;
    secondaryBody = shield;
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
