package com.mikilangelo.abysmal.components;

import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.models.definitions.EngineDef;
import com.mikilangelo.abysmal.models.definitions.LaserDef;
import com.mikilangelo.abysmal.models.definitions.ShipDef;
import com.mikilangelo.abysmal.models.definitions.TurretDef;
import com.mikilangelo.abysmal.models.game.extended.Asteroid;
import com.mikilangelo.abysmal.tools.BodLLoader;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
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

  public static void init() {
    GameScreen.world = new World(new Vector2(0, 0), true);
    GameScreen.world = new World(new Vector2(0, 0), true);
    generateDefender();
    generateHyperion();
    generateInvader();
  }

  public static ShipDef getShipDefinition(String shipName) {
    return shipDefinitions.get(shipNames.get(shipName));
  }

  public static void disposeAll() {
    Asteroid.dispose();
    shipDefinitions.clear();
    shipNames.clear();
    GameScreen.world.dispose();
  }

  private static void generateDefender() {
    ShipDef defender = new ShipDef();
    defender.name = "defender";
    defender.health = 45;
    defender.radarPower = 95;
    defender.maxZoom = 1.4f;
    // body
    defender.size = 2.25f;
    defender.density = 1.5f;
    defender.friction = 0.3f;
    defender.restitution = 0.85f;
    defender.bodyLoader = new BodLLoader(Gdx.files.internal("ships/defender/body.json"));
    defender.bodyScale = 2.3f;
    // dynamic
    defender.speedPower = 32.7f;
    defender.controlPower = 0.39f;
    defender.controlResistanceOnSpeed = 0.005f;
    defender.speedResistance = 0.0233f;
    defender.rotationResistance = 0.027f;
    // textures
    defender.bodyTexture = new Sprite(TexturesRepository.get("ships/defender/body.png"));
    defender.bodyTexture.setScale( defender.size / defender.bodyTexture.getHeight() );
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
      laserDefinition.sound = Gdx.audio.newSound(Gdx.files.internal("ships/defender/shot.wav"));
      laserDefinition.explosionTextures = new Array<>();
      laserDefinition.density = 1.9f;
    }

    for (byte i = 0; i < 11; i++) {
      laserDefinition.explosionTextures.add(new Sprite(TexturesRepository.get(
              "ships/defender/explosions/" + i + ".png")));
      laserDefinition.explosionTextures.get(i)
              .setScale( 1.97f / laserDefinition.explosionTextures.get(i).getHeight() );
    }
    defender.laserDefinition = laserDefinition;
    defender.lasersAmount = 2;
    defender.lasersDistance = 2.06f;
    defender.shotInterval = 311f;
    // turrets
    defender.turretDefinitions = new Array<>();
    // engines
    Array<EngineDef> engines = new Array<>();
    EngineDef engineBlue1 = new EngineDef();
    EngineDef engineBlue2 = new EngineDef();
    EngineDef engineRed1 = new EngineDef();
    EngineDef engineRed2 = new EngineDef();
    engineBlue1.particleTexture = new Sprite(TexturesRepository.get("ships/defender/kak1.png"));
    engineBlue2.particleTexture = engineBlue1.particleTexture;
    engineRed1.particleTexture = new Sprite(TexturesRepository.get("ships/defender/kak0.png"));
    engineRed2.particleTexture = engineRed1.particleTexture;
    engines.add(engineBlue1, engineBlue2, engineRed1, engineRed2);
    engineBlue1.positionX = engineRed1.positionX = -1.1f;
    engineBlue2.positionX = engineRed2.positionX = -1.1f;
    engineBlue1.positionY = engineRed1.positionY = 0.385f;
    engineBlue2.positionY = engineRed2.positionY = -0.385f;
    for (byte i = 0; i < engines.size; i ++) {
      final EngineDef e = engines.get(i);
      e.particleSpeedDispersion = 0.15f; // 10f;
      e.particlePositionDispersion = i > 1 ? 0.088f : 0.13f; // 10f;
      e.particleLifeTime = i > 1 ? 0.14f : 0.26f; // 7f;
      e.particleScale = i > 1 ? 0.009f : 0.0141f;
      e.particleSizeDispersion = 0.005f; // 0.011f;
      e.particleShipSpeedCoefficient = i > 1 ? 0.933f : 0.88f; // -0.05f;
      e.isTopLayer = i > 1;
    }
    defender.engineDefinitions = engines;

    shipNames.put(defender.name, shipDefinitions.size);
    shipDefinitions.add(defender);
  }

  private static void generateInvader() {
    ShipDef invader = new ShipDef();
    invader.name = "invader";
    invader.health = 65;
    invader.radarPower = 120;
    invader.maxZoom = 2.3f;
    // body
    invader.size = 2.57f;
    invader.density = 0.97f;
    invader.friction = 0.3f;
    invader.restitution = 0.85f;
    invader.bodyLoader = new BodLLoader(Gdx.files.internal("ships/invader/body.json"));
    invader.bodyScale = 6.3f;
    // dynamic
    invader.speedPower = 26.7f;
    invader.controlPower = 0.09f;
    invader.controlResistanceOnSpeed = -0.00001f;
    invader.speedResistance = 0.018f;
    invader.rotationResistance = 0.017f;
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
      laserDefinition.texture.setScale( 1.46f / laserDefinition.texture.getHeight() );
      laserDefinition.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      laserDefinition.sound = Gdx.audio.newSound(Gdx.files.internal("ships/defender/shot.wav"));
      laserDefinition.explosionTextures = new Array<>();
      laserDefinition.density = 1.9f;
    }

    for (byte i = 0; i < 11; i++) {
      laserDefinition.explosionTextures.add(new Sprite(TexturesRepository.get(
              "ships/hyperion/explosions/" + i + ".png")));
      laserDefinition.explosionTextures.get(i)
              .setScale( 1.93f / laserDefinition.explosionTextures.get(i).getHeight() );
    }
    invader.laserDefinition = laserDefinition;
    invader.lasersAmount = 1;
    invader.lasersDistance = 2.06f;
    invader.shotInterval = 211f;
    // turrets
    invader.turretDefinitions = new Array<>();
    // engines
    Array<EngineDef> engines = new Array<>();
    EngineDef e = new EngineDef();
    engines.add(e);
    e.positionX = -2.7f;
    e.positionY = 0;
    e.particleTexture = new Sprite(TexturesRepository.get("ships/invader/kak.png"));
    e.particleSpeedDispersion = 2.15f; // 10f;
    e.particlePositionDispersion = 0.1f; // 10f;
    e.particleLifeTime = 3.73f; // 7f;
    e.particleScale = 0.014f;
    e.particleSizeDispersion = 0.005f; // 0.011f;
    e.particleShipSpeedCoefficient = 0.9f; // -0.05f;
    invader.engineDefinitions = engines;

    shipNames.put(invader.name, shipDefinitions.size);
    shipDefinitions.add(invader);
  }

  private static void generateHyperion() {
    ShipDef hyperion = new ShipDef();
    hyperion.name = "hyperion";
    hyperion.health = 100f;
    hyperion.radarPower = 70;
    hyperion.maxZoom = 1.7f;
            // body
    hyperion.size = 4.15f;
    hyperion.density = 1.9f;
    hyperion.friction = 0.3f;
    hyperion.restitution = 0.85f;
    hyperion.bodyLoader = new BodLLoader(Gdx.files.internal("ships/hyperion/body.json"));
    hyperion.bodyScale = 7.43f;
    // dynamic
    hyperion.speedPower = 42.5f;
    hyperion.controlPower = 0.057f;
    hyperion.controlResistanceOnSpeed = 0f;
    hyperion.speedResistance = 0.0019f;
    hyperion.rotationResistance = 0.042f;
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
      mainLaser.texture.setScale( 0.63f / mainLaser.texture.getHeight() );
      mainLaser.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      mainLaser.sound = Gdx.audio.newSound(Gdx.files.internal("ships/hyperion/Laser2.wav"));
      mainLaser.explosionTextures = new Array<>();
      for (byte i = 0; i < 11; i++) {
        mainLaser.explosionTextures.add(new Sprite(TexturesRepository.get(
                "ships/hyperion/explosions/" + i + ".png")));
        mainLaser.explosionTextures.get(i)
                .setScale( 1.87f / mainLaser.explosionTextures.get(i).getHeight() );
      }
    }
    LaserDef smallLaser = new LaserDef(); {
      smallLaser.lifeTime = 1.7f;
      smallLaser.damage = 0.05f;
      smallLaser.touches = 1;
      smallLaser.density = 0.06f;
      smallLaser.impulse = 0.055f;
      smallLaser.texture = new Sprite(TexturesRepository.get("ships/hyperion/laser.png"));
      smallLaser.texture.setScale( 0.37f / mainLaser.texture.getHeight() );
      smallLaser.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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
    for (byte i = 0; i < 2; i++) {
      TurretDef smallTurret = new TurretDef();
      smallTurret.isAutomatic = true;
      smallTurret.positionX = -0.69f;
      smallTurret.positionY = i % 2 == 0 ? 0.83f : - 0.83f;
      smallTurret.lasersAmount = 2;
      smallTurret.rotationSpeed = 0.057f;
      smallTurret.lasersDistance = 0.1f;
      smallTurret.shotInterval = 127f;
      smallTurret.size = 0.33f;
      smallTurret.texture = new Sprite(TexturesRepository.get("ships/hyperion/turretSmall.png"));
      smallTurret.texture.setScale( mainTurret.size / mainTurret.texture.getHeight() );
      smallTurret.laserDefinition = smallLaser;
      hyperion.turretDefinitions.add(smallTurret);
    }
    // engines
    hyperion.engineDefinitions = new Array<>();

    shipNames.put(hyperion.name, shipDefinitions.size);
    shipDefinitions.add(hyperion);
  }

}
