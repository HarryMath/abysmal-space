package com.mikilangelo.abysmal.models.game.extended;

import com.mikilangelo.abysmal.components.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.models.definitions.LaserDef;
import com.mikilangelo.abysmal.models.game.animations.LaserExplosion;
import com.mikilangelo.abysmal.models.game.basic.DynamicObject;
import com.mikilangelo.abysmal.models.objectsData.LaserData;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Laser implements DynamicObject {
  final LaserDef definition;
  public Body body;
  public float angle;
  public float opacity = 1f;
  public boolean ended = false;
  final LaserData l;

  public Laser(LaserDef def, float x, float y, float angle, float speedX, float speedY, String shipId, float delay) {
    this.definition = def;
    this.angle = angle;
    BodyDef bodyDef = new BodyDef();
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape shape = new CircleShape();
    shape.setRadius(0.125f);
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    fixtureDef.density = def.density;
    fixtureDef.friction = 0.5f;
    fixtureDef.restitution = 0.5f;
    fixtureDef.shape = shape;
    body = GameScreen.world.createBody(bodyDef);
    body.createFixture(fixtureDef);
    final float impulseX = def.impulse * MathUtils.cos(angle) + speedX/27f * def.density;
    final float impulseY = def.impulse * MathUtils.sin(angle) + speedY/27f * def.density;
    body.setTransform(x + impulseX * delay, y + impulseY * delay, 0);
    body.applyLinearImpulse(impulseX, impulseY,body.getPosition().x, body.getPosition().y, true);
    l = new LaserData();
    l.shipId = shipId;
    l.damage = definition.damage;
    l.contactsCounter = 0;
    body.setUserData(l);
    body.setBullet(false);
    shape.dispose();
  }

  public Laser(LaserDef def, float x, float y, float angle, float impulseX, float impulseY, String shipId) {
    this(def, x, y, angle, impulseX, impulseY, shipId, 0);
  }

  @Override
  public void move(float delta) {
    if (!ended) {
      if (l.collision != null) {
        ExplosionsRepository.addLaserExplosion(new LaserExplosion(
                definition.explosionTextures, l.collision.x, l.collision.y, 0.015f
        ));
        l.collision = null;
        if (l.contactsCounter < definition.touches) {
          this.angle = Geometry.defineAngle(body.getLinearVelocity().x, body.getLinearVelocity().y, MathUtils.PI/2);
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