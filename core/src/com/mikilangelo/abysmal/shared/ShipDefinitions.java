package com.mikilangelo.abysmal.shared;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.mikilangelo.abysmal.shared.repositories.SoundsRepository;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.shared.defenitions.EngineDef;
import com.mikilangelo.abysmal.shared.defenitions.LaserDef;
import com.mikilangelo.abysmal.shared.defenitions.ShipDef;
import com.mikilangelo.abysmal.shared.defenitions.TurretDef;
import com.mikilangelo.abysmal.shared.tools.BodLLoader;
import com.mikilangelo.abysmal.shared.tools.Graphics;
import com.mikilangelo.abysmal.screens.game.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public abstract class ShipDefinitions {

  public static final Array<ShipDef> shipDefinitions = new Array<>();
  private static final Map<String, Integer> shipNames = new HashMap<>();
  private static final Array<Sprite> sampleExplosion = new Array<>();
  private static Texture sampleParticle;

  public static void init() {
    GameScreen.world = new World(new Vector2(0, 0), true);
    for (byte i = 0; i < 11; i++) {
      sampleExplosion.add(new Sprite(TexturesRepository.get("explosions/laser/" + i + ".png")));
      sampleExplosion.get(i).setScale( 2.2f / sampleExplosion.get(i).getHeight());
    }
    sampleParticle = TexturesRepository.get("ships/kak.png");
    generateDefender();
    generateHyperion();
    generateInvader();
    //generateXWing();
    generateRocinante();
    generateAlien();
  }

  public static ShipDef get(String shipName) {
    return shipDefinitions.get(shipNames.get(shipName));
  }

  public static ShipDef get(int index) {
    return shipDefinitions.get(index);
  }

  public static void disposeAll() {
    shipDefinitions.clear();
    shipNames.clear();
    sampleExplosion.clear();
  }

  private static void generateRocinante() {
    ShipDef rocinante = new ShipDef();
    rocinante.name = "rocinante";
    rocinante.health = 100f;
    rocinante.ammo = 9900;
    rocinante.radarPower = 90;
    rocinante.maxZoom = 2f;
    rocinante.minZoom = 1f;
    // body
    rocinante.size = 2.88f;
    rocinante.density = 1.33f;
    rocinante.friction = 0.3f;
    rocinante.restitution = 0.85f;
    rocinante.bodyLoader = new BodLLoader(Gdx.files.internal("ships/rocinante/body.json"));
    rocinante.bodyScale = 8.7f;
    rocinante.shieldRadius = 6.5f;
    // dynamic
    rocinante.speedPower = 105.5f;
    rocinante.controlPower = 0.11f;
    rocinante.speedResistance = 0.007f;
    rocinante.rotationControlResistance = 0.943f;
    rocinante.rotationResistance = 0.97f;
    // textures
    rocinante.bodyTexture = new Sprite(TexturesRepository.get("ships/rocinante/body.png"));
    rocinante.bodyTexture.setScale( rocinante.size / rocinante.bodyTexture.getHeight() );
    rocinante.engineAnimation = new Array<>();
    for (byte i = 0; i < 4; i++) {
      rocinante.engineAnimation.add(new Sprite(TexturesRepository.get("ships/rocinante/engine" + i + ".png")));
      rocinante.engineAnimation.get(i).setScale( rocinante.bodyTexture.getScaleY() );
    }
    rocinante.decorUnder = new Sprite(TexturesRepository.get("ships/rocinante/light.png"));
    rocinante.decorUnder.setScale(rocinante.bodyTexture.getScaleY());
    rocinante.frameFrequency = 0.06f;
    // lasers
    rocinante.laserDefinition = get("invader").laserDefinition;
    rocinante.lasersAmount = 1;
    rocinante.shotIntervalMs = 2500;
    // turrets
    rocinante.turretDefinitions = new Array<>();
    LaserDef mainLaser = new LaserDef(); {
      mainLaser.lifeTime = 2.1f;
      mainLaser.damage = 0.21f;
      mainLaser.touches = 2;
      mainLaser.density = 0.1f;
      mainLaser.impulse = 0.15f;
      mainLaser.texture = new Sprite(TexturesRepository.get("ships/rocinante/laser.png"));
      mainLaser.texture.setScale( 1.7f / mainLaser.texture.getHeight() );
      mainLaser.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      mainLaser.sound = SoundsRepository.getSound( "ships/rocinante/shot.mp3");
      mainLaser.explosionTextures = Graphics.changeColor(sampleExplosion, new Vector3(1f, 0.2f, 0));
      for (byte i = 0; i < 11; i++) {
        mainLaser.explosionTextures.get(i)
                .setScale( 1.87f / mainLaser.explosionTextures.get(i).getHeight() );
      }
    }
    TurretDef secondTurret = new TurretDef();
    TurretDef mainTurret = new TurretDef(); {
      mainTurret.isAutomatic = secondTurret.isAutomatic = false;
      mainTurret.positionX = 0.73f; mainTurret.positionY = 0f;
      secondTurret.positionX = -1.33f; secondTurret.positionY = 0f;
      mainTurret.lasersAmount = secondTurret.lasersAmount = 2;
      mainTurret.rotationSpeed = secondTurret.rotationSpeed = 0.1f;
      mainTurret.lasersDistance = secondTurret.lasersDistance = 0.25f;
      mainTurret.shotInterval = 37f;
      mainTurret.soundPlayInterval = secondTurret.soundPlayInterval = 75f;
      secondTurret.shotInterval = 57f;
      mainTurret.size = 1.9f;
      secondTurret.size = 1.7f;
      mainTurret.texture = secondTurret.texture = new Sprite(TexturesRepository.get("ships/rocinante/turret.png"));
      mainTurret.texture.setScale( mainTurret.size / mainTurret.texture.getHeight() );
      secondTurret.texture.setScale( secondTurret.size / mainTurret.texture.getHeight() );
      mainTurret.laserDefinition = secondTurret.laserDefinition = mainLaser;
      rocinante.turretDefinitions.add(mainTurret);//, secondTurret);
    }
    // engines
    rocinante.engineDefinitions = new Array<>();
    EngineDef e1 = new EngineDef(); {
      e1.particleTexture = new Sprite(TexturesRepository.get("ships/rocinante/kak.png"));
      e1.distBlendFunc = GL20.GL_ONE;
      e1.positionX = -3.25f;
      e1.positionY = 0;
      e1.particleSpeedDispersion = 1.5f; // 10f;
      e1.particlePositionDispersion = 0.008f; // 10f;
      e1.decayRate = 0.005f / 0.33f; // 7f;
      e1.particleScale = 0.047f;
      e1.particleSizeDispersion = 0.006f; // 0.011f;
      e1.particleShipSpeedCoefficient = 0.4f; // -0.05f;
      e1.particleAppearChance = 0.07f;
      e1.withTint = true;
      e1.lightDecay = 0.1f;
      e1.initialParticleOpacity = 1;
      e1.color[0] = 0.02f; e1.color[1] = 0.1f; e1.color[2] = 1;
      e1.isTopLayer = false;
      e1.isResizing = true;
    }
    EngineDef e2 = new EngineDef(); {
      e2.particleTexture = new Sprite(TexturesRepository.get("ships/rocinante/kak.png"));
      e2.distBlendFunc = GL20.GL_ONE;
      e2.positionX = -3.23f;
      e2.positionY = 0;
      e2.particleSpeedDispersion = 1.1f; // 10f;
      e2.particlePositionDispersion = 0.0035f; // 10f;
      e2.decayRate = 0.017f / 0.33f; // 7f;
      e2.particleScale = 0.047f;
      e2.particleSizeDispersion = 0.0059f; // 0.011f;
      e2.particleShipSpeedCoefficient = 0.43f; // -0.05f;
      e2.withTint = true;
      e2.lightDecay = 0.065f;
      e2.initialParticleOpacity = 1;
      e2.color[0] = 0.01f; e2.color[1] = 0.02f; e2.color[2] = 0.5f;
      e2.isTopLayer = true;
      e2.isResizing = true;
    }
    rocinante.engineDefinitions.add(e1, e2);
    addShip(rocinante);
  }

  private static void generateXWing() {
    ShipDef xWing = new ShipDef();
    xWing.name = "X-Wing";
    xWing.ammo = 100;
    xWing.health = 45;
    xWing.radarPower = 95;
    xWing.maxZoom = 1.4f;
    // body
    xWing.size = 2.263f;
    xWing.density = 1.5f;
    xWing.friction = 0.3f;
    xWing.restitution = 0.85f;
    xWing.bodyLoader = new BodLLoader(Gdx.files.internal("ships/defender/body.json"));
    xWing.bodyScale = 2.3f;
    xWing.shieldRadius = 2;
    // dynamic
    xWing.speedPower = 32.7f;
    xWing.controlPower = 0.901f;
    xWing.speedResistance = 0.0233f;
    xWing.rotationControlResistance = 0.91f;
    xWing.rotationResistance = 0.98f;
    // textures
    xWing.bodyTexture = new Sprite(TexturesRepository.get("ships/x-wing/body.png"));
    xWing.bodyTexture.setScale( xWing.size / xWing.bodyTexture.getHeight() );
    xWing.engineAnimation = new Array<>();
    for (byte i = 0; i < 4; i++) {
      xWing.engineAnimation.add(new Sprite(TexturesRepository.get("ships/x-wing/" + i + ".png")));
      xWing.engineAnimation.get(i).setScale( xWing.size / xWing.engineAnimation.get(i).getHeight() );
    }
    xWing.engineAnimation.add(xWing.engineAnimation.get(2));
    xWing.engineAnimation.add(xWing.engineAnimation.get(1));
    xWing.engineAnimation.add(xWing.engineAnimation.get(0));
    xWing.frameFrequency = 0.09f;
    // lasers
    LaserDef laserDefinition = new LaserDef(); {
      laserDefinition.impulse = 2.3f;
      laserDefinition.lifeTime = 3;
      laserDefinition.damage = 13;
      laserDefinition.touches = 2;
      laserDefinition.texture = new Sprite(TexturesRepository.get("ships/defender/laser.png"));
      laserDefinition.texture.setScale( 1.46f / laserDefinition.texture.getHeight() );
      laserDefinition.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      laserDefinition.sound = SoundsRepository.getSound( "ships/defender/shot.mp3");
      laserDefinition.explosionTextures = new Array<>();
      laserDefinition.density = 1.9f;
    }
    laserDefinition.explosionTextures = Graphics.changeColor(sampleExplosion, new Vector3(1, 0.04f, 0.1f));
    xWing.laserDefinition = laserDefinition;
    xWing.lasersAmount = 2;
    xWing.lasersDistance = 2.06f;
    xWing.shotIntervalMs = 311;
    // turrets
    xWing.turretDefinitions = new Array<>();
    // engines
    xWing.engineDefinitions = new Array<>();

    addShip(xWing);
  }

  private static void generateDefender() {
    ShipDef defender = new ShipDef();
    defender.name = "defender";
    defender.ammo = 100;
    defender.health = 45;
    defender.radarPower = 95;
    defender.maxZoom = 1.4f;
    // body
    defender.size = 2.263f;
    defender.density = 1.5f;
    defender.friction = 0.3f;
    defender.restitution = 0.85f;
    defender.bodyLoader = new BodLLoader(Gdx.files.internal("ships/defender/body.json"));
    defender.bodyScale = 2.3f;
    defender.shieldRadius = 2;
    // dynamic
    defender.speedPower = 32.7f;
    defender.controlPower = 0.79f;
    defender.speedResistance = 0.0233f;
    defender.rotationControlResistance = 0.91f;
    defender.rotationResistance = 0.98f;
    // textures
    defender.bodyTexture = new Sprite(TexturesRepository.get("ships/defender/body.png"));
    defender.bodyTexture.setScale( defender.size / defender.bodyTexture.getHeight() );
    defender.decorUnder = new Sprite(TexturesRepository.get("ships/defender/light.png"));
    defender.decorUnder.setScale(defender.bodyTexture.getScaleY());
    defender.engineAnimation = new Array<>();
    for (byte i = 0; i < 4; i++) {
      defender.engineAnimation.add(new Sprite(TexturesRepository.get("ships/defender/engine" + i + ".png")));
      defender.engineAnimation.get(i).setScale( defender.size / defender.engineAnimation.get(i).getHeight() );
    }
    defender.engineAnimation.add(defender.engineAnimation.get(2));
    defender.engineAnimation.add(defender.engineAnimation.get(1));
    defender.engineAnimation.add(defender.engineAnimation.get(0));
    defender.frameFrequency = 0.09f;
    // lasers
    LaserDef laserDefinition = new LaserDef(); {
      laserDefinition.impulse = 2.3f;
      laserDefinition.lifeTime = 3;
      laserDefinition.damage = 13;
      laserDefinition.touches = 2;
      laserDefinition.texture = new Sprite(TexturesRepository.get("ships/defender/laser.png"));
      laserDefinition.texture.setScale( 1.46f / laserDefinition.texture.getHeight() );
      laserDefinition.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      laserDefinition.sound = SoundsRepository.getSound( "ships/defender/shot.mp3");
      laserDefinition.explosionTextures = new Array<>();
      laserDefinition.density = 1.9f;
    }
    laserDefinition.explosionTextures = Graphics.changeColor(sampleExplosion, new Vector3(1, 0.04f, 0.1f));
    defender.laserDefinition = laserDefinition;
    defender.lasersAmount = 2;
    defender.lasersDistance = 2.06f;
    defender.shotIntervalMs = 311;
    // turrets
    defender.turretDefinitions = new Array<>();
    // engines
    Array<EngineDef> engines = new Array<>();
    EngineDef engineRed1 = new EngineDef();
    EngineDef engineRed2 = new EngineDef();
    engineRed1.particleTexture = new Sprite(sampleParticle);
    engineRed2.particleTexture = engineRed1.particleTexture;
    engines.add(engineRed1, engineRed2);
    engineRed1.positionX = engineRed2.positionX = -0.9f;
    engineRed1.positionY = 0.385f;
    engineRed2.positionY = -0.385f;
    for (byte i = 2; i < engines.size + 2; i ++) {
      final EngineDef e = engines.get(i - 2);
      e.particleSpeedDispersion = 0.15f; // 10f;
      e.particlePositionDispersion = i > 1 ? 0.088f : 0.13f; // 10f;
      e.decayRate = 0.016f / 0.17f;
      e.particleScale = 0.009f;
      e.particleSizeDispersion = 0.005f; // 0.011f;
      e.particleShipSpeedCoefficient = 0.88f;
      e.isTopLayer = i > 1;
      if (i > 1) {
        e.withTint = true;
        e.color[0] = 1; e.color[1] = 0; e.color[2] = 0.2f;
      }
    }
    defender.engineDefinitions = engines;
    addShip(defender);
  }

  private static void generateInvader() {
    ShipDef invader = new ShipDef();
    invader.name = "invader";
    invader.health = 65;
    invader.ammo = 110;
    invader.radarPower = 120;
    invader.maxZoom = 2.3f;
    // body
    invader.size = 2.587f;
    invader.density = 0.97f;
    invader.friction = 0.3f;
    invader.restitution = 0.85f;
    invader.bodyLoader = new BodLLoader(Gdx.files.internal("ships/invader/body.json"));
    invader.bodyScale = 6.3f;
    invader.shieldRadius = 2.9f;
    // dynamic
    invader.speedPower = 29.7f;
    invader.controlPower = 0.09f;
    invader.speedResistance = 0.0176f;
    invader.rotationControlResistance = 0.98f;
    invader.rotationResistance = 0.982f;
    // textures
    invader.bodyTexture = new Sprite(TexturesRepository.get("ships/invader/body.png"));
    invader.bodyTexture.setScale( invader.size / invader.bodyTexture.getHeight() );
    invader.engineAnimation = new Array<>();
    for (byte i = 0; i < 0; i++) {
      invader.engineAnimation.add(new Sprite(TexturesRepository.get("ships/invader/engine" + i + ".png")));
      invader.engineAnimation.get(i).setScale( invader.size / invader.engineAnimation.get(i).getHeight() );
    }
    invader.frameFrequency = 0.1f;
    // lasers
    LaserDef laserDefinition = new LaserDef(); {
      laserDefinition.impulse = 5.5f;
      laserDefinition.lifeTime = 3;
      laserDefinition.damage = 17;
      laserDefinition.touches = 3;
      laserDefinition.texture = new Sprite(TexturesRepository.get("ships/invader/laser.png"));
      laserDefinition.texture.setScale( 5.3f / laserDefinition.texture.getHeight() );
      laserDefinition.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      laserDefinition.sound = SoundsRepository.getSound( "ships/invader/shot.mp3");
      laserDefinition.explosionTextures = new Array<>();
      laserDefinition.density = 1.9f;
    }

    laserDefinition.explosionTextures = Graphics
            .changeColor(sampleExplosion, new Vector3(0.08f, 0.59f, 0.93f));
    for (byte i = 0; i < 11; i++) {
      laserDefinition.explosionTextures.get(i)
              .setScale( 2.27f / laserDefinition.explosionTextures.get(i).getHeight() );
    }
    invader.laserDefinition = laserDefinition;
    invader.lasersAmount = 1;
    invader.laserX = 3;
    invader.lasersDistance = 2.06f;
    invader.shotIntervalMs = 241;
    // turrets
    invader.turretDefinitions = new Array<>();
    // engines
    Array<EngineDef> engines = new Array<>();
    EngineDef e = new EngineDef();
    engines.add(e);
    e.positionX = -2.07f;
    e.positionY = 0;
    e.particleTexture = new Sprite(TexturesRepository.get("ships/invader/kak.png"));
    e.distBlendFunc = GL20.GL_ONE;
    e.particleSpeedDispersion = 3.15f; // 10f;
    e.particlePositionDispersion = 0.1f; // 10f;
    e.decayRate = 0.016f / 11f; // 7f;
    e.particleScale = 0.011f;
    e.particleSizeDispersion = 0.0036f; // 0.011f;
    e.particleShipSpeedCoefficient = 0.5f;
    invader.engineDefinitions = engines;
    addShip(invader);
  }

  private static void generateAlien() {
    ShipDef alien = new ShipDef();
    alien.name = "alien";
    alien.health = 95;
    alien.ammo = 450;
    alien.radarPower = 120;
    alien.maxZoom = 2.3f;
    // body
    alien.size = 4.137f;
    alien.density = 0.87f;
    alien.friction = 0.3f;
    alien.restitution = 0.85f;
    alien.bodyLoader = new BodLLoader(Gdx.files.internal("ships/alien/body.json"));
    alien.bodyScale = 6.87f;
    alien.shieldRadius = 3.9f;
    // dynamic
    alien.speedPower = 347.7f;
    alien.controlPower = 0.6f;
    alien.speedResistance = 0.0727f;
    alien.rotationControlResistance = 0.9f;
    alien.rotationResistance = 0.911f;
    // textures
    alien.bodyTexture = new Sprite(TexturesRepository.get("ships/alien/body.png"));
    alien.bodyTexture.setScale( alien.size / alien.bodyTexture.getHeight() );
    alien.decorOver = new Sprite(TexturesRepository.get("ships/alien/decor.png"));
    alien.decorOver.setScale( alien.bodyTexture.getScaleY() );
    alien.engineAnimation = new Array<>();
    for (byte i = 0; i < 0; i++) {
      alien.engineAnimation.add(new Sprite(TexturesRepository.get("ships/alien/engine" + i + ".png")));
      alien.engineAnimation.get(i).setScale( alien.size / alien.engineAnimation.get(i).getHeight() );
    }
    alien.frameFrequency = 0.1f;
    // lasers
    LaserDef laserDefinition = new LaserDef(); {
      laserDefinition.impulse = 2.3f;
      laserDefinition.lifeTime = 3;
      laserDefinition.damage = 7;
      laserDefinition.touches = 2;
      laserDefinition.texture = new Sprite(TexturesRepository.get("ships/alien/laser.png"));
      laserDefinition.texture.setScale( 0.9f / laserDefinition.texture.getHeight() );
      laserDefinition.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      laserDefinition.sound = SoundsRepository.getSound( "ships/alien/shot.mp3");
      laserDefinition.explosionTextures = new Array<>();
      laserDefinition.density = 1.9f;
    }

    laserDefinition.explosionTextures = Graphics
            .changeColor(sampleExplosion, new Vector3(0.15f, 1, 0.05f));
    for (byte i = 0; i < 11; i++) {
      laserDefinition.explosionTextures.get(i)
              .setScale( 2.47f / laserDefinition.explosionTextures.get(i).getHeight() );
    }
    alien.laserDefinition = laserDefinition;
    alien.lasersAmount = 2;
    alien.lasersDistance = 1.06f;
    alien.shotIntervalMs = 211;
    // turrets
    alien.turretDefinitions = new Array<>();
    // engines
    Array<EngineDef> engines = new Array<>();
    EngineDef engine1 = new EngineDef();
    EngineDef engine2 = new EngineDef();
    engine1.particleTexture = new Sprite(TexturesRepository.get("ships/alien/kak.png"));
    engine2.particleTexture = engine1.particleTexture;
    engines.add(engine1, engine2);
    engine1.positionX = -2.1f;
    engine2.positionX = -2.1f;
    engine1.positionY = 0.477f;
    engine2.positionY = -0.477f;
    for (byte i = 0; i < engines.size; i ++) {
      final EngineDef e = engines.get(i);
      e.particleSpeedDispersion = 0.08f; // 10f;
      e.particlePositionDispersion = 0.13f; // 10f;
      e.decayRate = 0.016f / 0.28f; // 7f;
      e.particleScale = 0.014f;
      e.particleSizeDispersion = 0.003f; // 0.011f;
      e.particleShipSpeedCoefficient = 0.85f; // -0.05f;
      e.isTopLayer = false;
    }
    alien.engineDefinitions = engines;
    addShip(alien);
  }

  private static void generateHyperion() {
    ShipDef hyperion = new ShipDef();
    hyperion.name = "hyperion";
    hyperion.health = 100f;
    hyperion.ammo = 9900;
    hyperion.radarPower = 70;
    hyperion.maxZoom = 1.9f;
    hyperion.minZoom = 1f;
            // body
    hyperion.size = 4.15f;
    hyperion.density = 1.9f;
    hyperion.friction = 0.3f;
    hyperion.restitution = 0.85f;
    hyperion.bodyLoader = new BodLLoader(Gdx.files.internal("ships/hyperion/body.json"));
    hyperion.bodyScale = 7.43f;
    hyperion.shieldRadius = 4.1f;
    // dynamic
    hyperion.speedPower = 42.5f;
    hyperion.controlPower = 0.057f;
    hyperion.speedResistance = 0.0019f;
    hyperion.rotationControlResistance = 0.958f;
    hyperion.rotationResistance = 0.96f;
    // textures
    hyperion.bodyTexture = new Sprite(TexturesRepository.get("ships/hyperion/body.png"));
    hyperion.bodyTexture.setScale( hyperion.size / hyperion.bodyTexture.getHeight() );
    hyperion.engineAnimation = new Array<>();
    for (byte i = 0; i < 3; i++) {
      hyperion.engineAnimation.add(new Sprite(TexturesRepository.get("ships/hyperion/engine" + i + ".png")));
      hyperion.engineAnimation.get(i).setScale( hyperion.bodyTexture.getScaleY() );
    }
    hyperion.frameFrequency = 0.06f;
    // lasers
    hyperion.laserDefinition = null;
    hyperion.lasersAmount = 0;
    // turrets
    hyperion.turretDefinitions = new Array<>();
    LaserDef mainLaser = new LaserDef(); {
      mainLaser.lifeTime = 3f;
      mainLaser.damage = 0.37f;
      mainLaser.touches = 1;
      mainLaser.density = 0.2f;
      mainLaser.impulse = 0.3f;
      mainLaser.texture = new Sprite(TexturesRepository.get("ships/hyperion/laser.png"));
      mainLaser.texture.setScale( 1.43f / mainLaser.texture.getHeight() );
      mainLaser.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      mainLaser.sound = SoundsRepository.getSound( "ships/hyperion/shot.mp3");
      mainLaser.explosionTextures = Graphics.changeColor(sampleExplosion, new Vector3(0.09f, 0.02f, 1f));
      for (byte i = 0; i < 11; i++) {
        mainLaser.explosionTextures.get(i)
                .setScale( 1.87f / mainLaser.explosionTextures.get(i).getHeight() );
      }
    }
    LaserDef smallLaser = new LaserDef(); {
      smallLaser.lifeTime = 1.7f;
      smallLaser.damage = 0.2f; // 0.05f;
      smallLaser.touches = 1;
      smallLaser.density = 0.05f;
      smallLaser.impulse = 0.058f;
      smallLaser.texture = new Sprite(TexturesRepository.get("ships/hyperion/laser.png"));
      smallLaser.texture.setScale( 0.9f / mainLaser.texture.getHeight() );
      smallLaser.sound = mainLaser.sound;
      smallLaser.explosionTextures = mainLaser.explosionTextures;
    }
    TurretDef mainTurret = new TurretDef(); {
      mainTurret.isAutomatic = false;
      mainTurret.positionX = 0.75f; mainTurret.positionY = 0f;
      mainTurret.lasersAmount = 2;
      mainTurret.rotationSpeed = 0.057f;
      mainTurret.lasersDistance = 0.28f;
      mainTurret.shotInterval = 177f;
      mainTurret.size = 0.66f;
      mainTurret.texture = new Sprite(TexturesRepository.get("ships/hyperion/turret.png"));
      mainTurret.texture.setScale( mainTurret.size / mainTurret.texture.getHeight() );
      mainTurret.laserDefinition = mainLaser;
      hyperion.turretDefinitions.add(mainTurret);
    }
    for (byte i = 0; i < 4; i++) {
      TurretDef smallTurret = new TurretDef();
      smallTurret.isAutomatic = true;
      smallTurret.positionX = i > 1 ? -0.69f : 1.3f;
      smallTurret.positionY = i % 2 == 0 ? 0.83f : - 0.83f;
      smallTurret.lasersAmount = 1; // 2;
      smallTurret.rotationSpeed = 0.057f;
      smallTurret.lasersDistance = 0; //0.1f;
      smallTurret.shotInterval = 127f;
      smallTurret.size = 0.33f;
      smallTurret.texture = new Sprite(TexturesRepository.get("ships/hyperion/turretSmall.png"));
      smallTurret.texture.setScale( mainTurret.size / mainTurret.texture.getHeight() );
      smallTurret.laserDefinition = smallLaser;
      hyperion.turretDefinitions.add(smallTurret);
    }
    // engines
    hyperion.engineDefinitions = new Array<>();
    addShip(hyperion);
  }

  private static void addShip(ShipDef definition) {
    definition.id = shipDefinitions.size;
    shipNames.put(definition.name, shipDefinitions.size);
    shipDefinitions.add(definition);
  }

}
