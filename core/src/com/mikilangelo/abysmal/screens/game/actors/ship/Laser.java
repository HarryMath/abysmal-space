package com.mikilangelo.abysmal.screens.game.actors.ship;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mikilangelo.abysmal.shared.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.shared.defenitions.LaserDef;
import com.mikilangelo.abysmal.screens.game.actors.decor.animations.LaserExplosion;
import com.mikilangelo.abysmal.screens.game.actors.basic.DynamicObject;
import com.mikilangelo.abysmal.screens.game.objectsData.LaserData;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.ShotData;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.screens.game.GameScreen;

public class Laser implements DynamicObject {

  public static ShotData lastShotData;

  final LaserDef definition;
  private final LaserData l;
  public Body body;
  public float angle;
  public float opacity = 1f;
  public boolean ended = false;

  public Laser(LaserDef def, ShotData shotData, short bodyId) {
    final float delta = (System.currentTimeMillis() - shotData.timestamp) * 0.001f;
    this.definition = def;
    this.angle = shotData.angle;
    BodyDef bodyDef = new BodyDef();
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape shape = new CircleShape();
    shape.setRadius(0.125f);
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    fixtureDef.density = def.density;
    fixtureDef.friction = 0.5f;
    fixtureDef.restitution = 0.5f;
    fixtureDef.shape = shape;
    fixtureDef.filter.groupIndex = bodyId;
    body = GameScreen.world.createBody(bodyDef);
    body.createFixture(fixtureDef);
    if (delta > 0.007f) {
      final float mass = body.getMass();
      shotData.x += shotData.impulseX / mass * delta;
      shotData.y += shotData.impulseY / mass * delta;
      this.opacity -= delta / definition.lifeTime;
    } else {
      lastShotData = shotData;
    }
    body.setTransform(shotData.x, shotData.y, 0);
    body.applyLinearImpulse(shotData.impulseX, shotData.impulseY, shotData.x, shotData.y, true);
    l = new LaserData();
    l.shipId = shotData.generationId;
    l.damage = definition.damage;
    l.contactsCounter = 0;
    body.setUserData(l);
    body.setBullet(false);
    shape.dispose();
  }

  public Laser(LaserDef def, float x, float y, float angle, float speedX, float speedY, String shipId, short bodyId) {
    this(def, new ShotData(
            x, y, angle,
            def.impulse * MathUtils.cos(angle) + speedX / 27f * def.density,
            def.impulse * MathUtils.sin(angle) + speedY / 27f * def.density,
            0,
            System.currentTimeMillis() + 1,
            shipId
    ), bodyId);
  }

  @Override
  public void move(float delta) {
    if (!ended) {
      if (l.collision != null) {
        ExplosionsRepository.addLaserExplosion(new LaserExplosion(
                definition.explosionTextures, l.collision.x, l.collision.y, 0.017f
        ));
        l.collision = null;
        if (l.contactsCounter < definition.touches) {
          this.angle = CalculateUtils.defineAngle(body.getLinearVelocity().x, body.getLinearVelocity().y, MathUtils.PI / 2);
        }
      }
      if (this.opacity <= 0 || l.contactsCounter >= definition.touches) {
        destroyBody();
        return;
      }
      if (this.opacity > 0) {
        this.opacity -= delta / definition.lifeTime;
      }
    }
  }

  @Override
  public void draw(Batch batch) {
    if (!ended && opacity > 0) {
      this.definition.texture.setCenter(body.getPosition().x, body.getPosition().y);
      this.definition.texture.setAlpha(opacity);
      this.definition.texture.setRotation(angle * MathUtils.radiansToDegrees);
      this.definition.texture.draw(batch);
    }
  }

  public void destroyBody() {
    if (body != null) {
      GameScreen.world.destroyBody(body);
      body = null;
    }
    opacity = 0;
    ended = true;
  }
}
