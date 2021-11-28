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
import com.mikilangelo.abysmal.ui.Joystick;
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

  private final float controlSpeedResistance;
  private final float simpleSpeedResistance;
  private final float shieldRadius;
  private final float shieldSize;
  private float shieldScale;
  private final Sprite shieldTexture = new Sprite(TexturesRepository.get("things/shield.png"));
  private final Sprite shieldTouchTexture = new Sprite(TexturesRepository.get("things/shieldTouch.png"));
  private final Array<ShieldTouch> shieldTouches = new Array<>();

  public Ship(ShipDef def, float x, float y) {
    this(def, x, y, true, x, y);
  }


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
    this.controlSpeedResistance = (0.998f - definition.speedResistance / 2);
    this.simpleSpeedResistance = 0.9995f - definition.speedResistance * definition.speedResistance;
    this.x = x; this.y = y;
    this.distance =isPlayer ? 0 : Geometry.distance(playerX, playerY, x, y);
    shieldRadius = 0.63f * definition.size * (float) Math.hypot(1, definition.bodyTexture.getWidth() / definition.bodyTexture.getHeight());
    shieldSize = shieldRadius * 2 / shieldTexture.getHeight();
    shieldScale = shieldSize;
  }

  public void control(float touchX, float touchY, float centerX, float centerY, float delta) {
    newAngle = Geometry.defineAngle(touchX - centerX, touchY - centerY, newAngle);
    float touchDistance = Geometry.distance(touchX, touchY, centerX, centerY);
    touchDistance = touchDistance > Joystick.radius ? 1 : touchDistance / Joystick.radius;
    this.applyImpulse(touchDistance, delta, this.distance < SCREEN_WIDTH);
    if (angle < 0 || newAngle < 0 || angle > MathUtils.PI2 || newAngle > MathUtils.PI2) {
      System.out.println("WARNING! angles incorrect");
      System.out.println("newAngle: " + newAngle);
      System.out.println("angles: " + angle);
      System.exit(-11);
    }
    if (angle != newAngle) {
      if (angle <= newAngle + definition.controlPower / 5f && angle >= newAngle - definition.controlPower / 5f) {
        this.body.setAngularVelocity(this.body.getAngularVelocity() * 0.82f);
      } else {
        final float rotationLeft = (angle - newAngle + MathUtils.PI2) % MathUtils.PI2;
        final float rotationRight = (newAngle - angle + MathUtils.PI2) % MathUtils.PI2;
        if (rotationLeft < rotationRight) {
          this.rotate(- rotationLeft / MathUtils.PI2 * 0.35f - 0.65f);
        } else {
          this.rotate(rotationLeft / MathUtils.PI2 * 0.35f + 0.65f);
        }
      }
    }
  }

  // power: [0, 1]
  public void applyImpulse(float power, float delta, boolean withParticles) {
    primaryBody.applyLinearImpulse(
            power * MathUtils.cos(angle) * definition.speedPower * (delta / 2 + 1/120f),
            power * MathUtils.sin(angle) * definition.speedPower * (delta / 2 + 1/120f),
            primaryBody.getPosition().x,
            primaryBody.getPosition().y,
            true);
    primaryBody.setLinearVelocity(
            primaryBody.getLinearVelocity().x * controlSpeedResistance,
            primaryBody.getLinearVelocity().y * controlSpeedResistance);
    if (withParticles) kak();
  }

  public void rotate(float direction) {
    final float coef = Math.min(primaryBody.getLinearVelocity().len() * definition.controlResistanceOnSpeed, 0.5f);
    this.body.setAngularVelocity(this.body.getAngularVelocity() + direction * definition.controlPower);
    if (!underControl) {
      primaryBody.setLinearVelocity(
              body.getLinearVelocity().x * controlSpeedResistance,
              body.getLinearVelocity().y * controlSpeedResistance);
    }
    this.body.setAngularVelocity(this.body.getAngularVelocity() * (0.999f - coef - definition.rotationResistance));
  }

  public void move(float delta, float playerX, float playerY) {
    move(delta);
    this.distance = Geometry.distance(playerX, playerY, x, y);
  }

  public void move(float delta) {
    x = primaryBody.getPosition().x;
    y = primaryBody.getPosition().y;
    this.angle = (this.body.getAngle() + MathUtils.PI2) % MathUtils.PI2;
    this.body.setAngularVelocity(this.body.getAngularVelocity() * (1 - definition.rotationResistance));
    primaryBody.setLinearVelocity(
            primaryBody.getLinearVelocity().x * simpleSpeedResistance,
            primaryBody.getLinearVelocity().y * simpleSpeedResistance);
    // primaryBody.setTransform(x, y, angle);
    secondaryBody.setTransform(x, y, angle);
    secondaryBody.setLinearVelocity(primaryBody.getLinearVelocity());
    for (Turret t : turrets) {
      t.move(angle, x, y, delta);
    }
  }

  public void shotDirectly(float soundScale, float x, float y, float sX, float sY, long deltaMillis) {
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
    definition.laserDefinition.sound.play(soundScale);
    this.lastShotTime = newShotTime;
  }

  public void shot(float soundScale) {
    if (definition.lasersAmount < 1 && definition.turretDefinitions.size == 0) {
      return;
    }
    for (byte i = 0; i < turrets.size; i++) {
      turrets.get(i).shot(this, soundScale);
    }
    final long newShotTime = TimeUtils.millis();
    if (turrets.size > 0 || (newShotTime - lastShotTime) < definition.shotInterval) {
      return;
    }
    if (distance < 0.1f) {
      GameScreen.enemiesProcessor.shot();
    }
    shotDirectly(soundScale,
            this.body.getPosition().x,
            this.body.getPosition().y,
            this.body.getLinearVelocity().x,
            this.body.getLinearVelocity().y,
            0);
  }

  public void shot() {
    shot(Math.max((130 - distance) / 131, 0) * ((130 - distance) / 135));
  }

  public void draw(Batch batch, float delta) {
    x = body.getPosition().x;
    y = body.getPosition().y;
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
    body.setTransform(x, y, MathUtils.PI / 2);
    ShipData data = new ShipData();
    data.id = generationId;
    data.health = definition.health;
    this.body.setUserData(data);
    primaryBody = body;
    createShield(world);
  }

  private void createShield(World world) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.x = x; bodyDef.position.y = y;
    CircleShape shape = new CircleShape();
    shape.setRadius(shieldRadius);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = body.getMass() / (shieldRadius * shieldRadius * MathUtils.PI);
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
