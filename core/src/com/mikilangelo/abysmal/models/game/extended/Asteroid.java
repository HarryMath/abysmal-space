package com.mikilangelo.abysmal.models.game.extended;

import com.mikilangelo.abysmal.components.repositories.AsteroidsRepository;
import com.mikilangelo.abysmal.components.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.components.repositories.SoundsRepository;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.models.game.animations.Explosion;
import com.mikilangelo.abysmal.models.game.basic.DynamicObject;
import com.mikilangelo.abysmal.models.objectsData.AsteroidData;
import com.mikilangelo.abysmal.tools.BodLLoader;
import com.mikilangelo.abysmal.tools.Geometry;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;

public class Asteroid implements DynamicObject {

  private static final float healthWeight = 11.7f;
  private static Sound explosionSound;
  private static final float[] bodyScales = new float[] {
          0.42f, 0.33f, 0.41f, 0.51f, 0.62f,
          1.55f,
          2.30f, 3.51f, 2.40f, 3.9f, 3, 3, 3.88f
  };
  private static final  float[] sqrtmass = new float[bodyScales.length];
  private static final float textureScale = 0.0021f;
  public static final byte bigAmount = 7;
  public static final byte mediumAmount = 1;
  public static final byte smallAmount = 5;
  public static final int typesAmount = bigAmount + smallAmount + mediumAmount;

  private static Array<Sprite> textures;
  private static Array<BodLLoader> bodies;
  private static final Array<Sprite> explosion = new Array<>();
  private static final Array<Sprite> stoneExplosion = new Array<>();
  private static boolean initialized = false;


  public int asteroidTypeId;
  private boolean bodyLoaded = false;
  public boolean destroyed = false;
  public Body body;
  private final AsteroidData bodyData;
  public float x, y;

  public Asteroid(final int asteroidTypeId, final float x, final float y) {
    this.asteroidTypeId = asteroidTypeId;
    bodyData = loadBody(x, y);
    if (sqrtmass[asteroidTypeId] == 0) {
      sqrtmass[asteroidTypeId] = (float) Math.sqrt(body.getMass());
    }
  }

  public static void init() {
    if (!initialized) {
      textures = new Array<>();
      bodies = new Array<>();
      loadBodies(smallAmount, "small");
      loadBodies(mediumAmount, "medium");
      loadBodies(bigAmount, "big");
      for (byte i = 0; i < 6; i++) {
        Sprite s = new Sprite(TexturesRepository.get("explosions/small/" + i + ".png"));
        s.setScale( GameScreen.SCREEN_HEIGHT * textureScale * 0.41f);
        explosion.add(s);
      }
      for (byte i = 0; i < 4; i++) {
        Sprite s = new Sprite(TexturesRepository.get("explosions/stone/" + i + ".png"));
        s.setScale( GameScreen.SCREEN_HEIGHT * textureScale * 1.1f);
        stoneExplosion.add(s);
      }
      explosionSound = SoundsRepository.getSound("explosions/stone/sound.mp3");
      initialized = true;
    }
  }

  public float getSqrtMass() {
    return sqrtmass[asteroidTypeId];
  }

  private static void loadBodies(byte amount, String type) {
    for (byte i = 0; i < amount; i++) {
      Sprite s = new Sprite(TexturesRepository.get("asteroids/" + type + "/" + i + "/texture.png"));
      s.setScale( GameScreen.SCREEN_HEIGHT * textureScale);
      textures.add(s);
      bodies.add(new BodLLoader(Gdx.files.internal("asteroids/" + type + "/" + i + "/body.json")));
    }
  }

  public AsteroidData loadBody(float x, float y) {
    if (!initialized) {
      return null;
    }
    this.x = x; this.y = y;
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = 1.1f;
    fixtureDef.friction = 0.7f;
    fixtureDef.restitution = 0.5f;
    body = GameScreen.world.createBody(bodyDef);
    bodies.get(asteroidTypeId).attachFixture(body, "Name", fixtureDef, bodyScales[asteroidTypeId]);
    body.setTransform(x, y, MathUtils.random(0, MathUtils.PI2));
    body.setAwake(false);
    AsteroidData asteroidData = new AsteroidData();
    asteroidData.health = healthWeight * (float) Math.pow(body.getMass(), 0.6f) * MathUtils.random(1.1f, 3f);
    body.setUserData(asteroidData);
    bodyLoaded = true;
    return asteroidData;
  }

  public void destroyBody() {
    GameScreen.world.destroyBody(this.body);
    this.body = null;
  }

  @Override
  public void move(float delta) {
    if (this.bodyLoaded && !destroyed) {
      final Vector2 speed = this.body.getLinearVelocity();
      this.body.setAngularVelocity(this.body.getAngularVelocity() * 0.99f);
      body.setLinearVelocity(speed.scl(0.997f));
      if (bodyData.health <= 0) {
        destroyed = true;
        x = this.body.getPosition().x;
        y = this.body.getPosition().y;
        ExplosionsRepository.addExplosion(new Explosion(stoneExplosion, x, y, 0.05f));
        if (asteroidTypeId >= smallAmount) {
          final float distance = Geometry.distance(x, y, GameScreen.camera.X, GameScreen.camera.Y);
          if (distance < 50) {
            explosionSound.play(1 - distance / 50);
          }
          final int amount = 4 + MathUtils.random(1, 4);
          for (byte i = 0; i < amount; i++) {
            final float xSkew = MathUtils.random(-0.7f, 0.7f);
            final float ySkew = MathUtils.random(-0.7f, 0.7f);
            final int type = MathUtils.random(0, smallAmount - 1);
            Asteroid newAsteroid = new Asteroid(type, x + xSkew, y + ySkew);
            newAsteroid.body.applyAngularImpulse(MathUtils.random(-0.9f, 0.9f), true);
            newAsteroid.body.applyLinearImpulse(
                    (MathUtils.random(-0.05f, 0.05f) + speed.x * 0.5f + xSkew * 1.5f),
                    (MathUtils.random(-0.05f, 0.05f) + speed.y * 0.5f + ySkew * 1.5f),
                    x + xSkew, y + ySkew, true);
            AsteroidsRepository.add(newAsteroid);
          }
          // ExplosionsRepository.addExplosion(new Explosion(explosion, x, y, 0.033f));
        }
      }
    }
  }

  @Override
  public void draw(Batch batch) {
    if (!destroyed) {
      textures.get(asteroidTypeId).setRotation(this.body.getAngle() * MathUtils.radiansToDegrees);
      textures.get(asteroidTypeId).setCenter(this.body.getPosition().x, this.body.getPosition().y);
      textures.get(asteroidTypeId).draw(batch);
    }
  }

  public static void dispose() {
    textures.clear();
    bodies.clear();
    stoneExplosion.clear();
    explosion.clear();
    initialized = false;
  }
}
