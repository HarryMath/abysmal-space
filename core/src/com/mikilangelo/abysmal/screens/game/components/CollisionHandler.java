package com.mikilangelo.abysmal.screens.game.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.mikilangelo.abysmal.screens.game.objectsData.AsteroidData;
import com.mikilangelo.abysmal.screens.game.objectsData.DestroyableObjectData;
import com.mikilangelo.abysmal.screens.game.objectsData.IdentityData;
import com.mikilangelo.abysmal.screens.game.objectsData.LaserData;
import com.mikilangelo.abysmal.screens.game.objectsData.ShieldData;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.shared.tools.Logger;

public class CollisionHandler implements ContactListener {

  private final String playerId;

  public CollisionHandler(String playerId) {
    this.playerId = playerId;
  }


  @Override
  public void beginContact(Contact contact) {
  }

  @Override
  public void endContact(Contact contact) {
  }

  @Override
  public void preSolve(Contact contact, Manifold oldManifold) {
    Body bodyA = contact.getFixtureA().getBody();
    Body bodyB = contact.getFixtureB().getBody();
    final Object bodyAData = bodyA.getUserData();
    final Object bodyBData = bodyB.getUserData();
    if (bodyAData != null && bodyBData != null) {
      String shipAId = (bodyAData instanceof IdentityData) ?
              ((IdentityData) bodyAData).getId() : null;
      String shipBId = (bodyBData instanceof IdentityData) ?
              ((IdentityData) bodyBData).getId() : null;
      if (shipAId != null && shipAId.equalsIgnoreCase(shipBId)) {
        Logger.log(this, "preSolve", "ignored contact");
        contact.setEnabled(false);
        return;
      }
    }
    handleCollision(contact, bodyA, bodyB, bodyAData, bodyBData);
    handleCollision(contact, bodyB, bodyA, bodyBData, bodyAData);
  }

  private void handleCollision(Contact contact, Body bodyA, Body bodyB, Object bodyAData, Object bodyBData) {
    if (contact.isEnabled() && (bodyAData != null)) {
      if (bodyAData instanceof LaserData) {
        LaserData l = (LaserData) bodyAData;
        l.collision = contact.getWorldManifold().getPoints()[0];
        l.contactsCounter++;
      }
      else if (bodyAData instanceof DestroyableObjectData) {
        final float speed = bodyB.getLinearVelocity().sub(bodyA.getLinearVelocity()).len();
        float damage = speed > 3 ? (float) (Math.pow(bodyB.getMass(), 0.3) *
                Math.pow(bodyB.getLinearVelocity()
                        .sub(bodyA.getLinearVelocity().scl(0.5f))
                        .len(), 1.71)) / 17f : 0;
        if (bodyBData instanceof LaserData) {
          LaserData l = (LaserData) bodyBData;
          damage = (l.damage * 5 + damage) / 5.5f;
          if (l.shipId.equals(playerId) && bodyAData instanceof IdentityData) {
            ((IdentityData) bodyAData).setPlayerFocus();
          }
        }
        // System.out.println("\nimpulse: " + bodyB.getMass() * bodyB.getLinearVelocity().len());
        // System.out.println("damage: " + damage);
        ((DestroyableObjectData) bodyAData).damage(damage);
        if (((DestroyableObjectData) bodyAData).getHealth() < 0) {
          contact.setEnabled(false);
        }
      }
      else if (bodyAData instanceof ShieldData) {
        final float speed = bodyB.getLinearVelocity().sub(bodyA.getLinearVelocity()).len();
        final float angle = MathUtils.radiansToDegrees *
                CalculateUtils.defineAngle(contact.getWorldManifold().getPoints()[0], bodyA.getPosition());
        ((ShieldData) bodyAData).touch(angle, speed * 0.02f);
      }
    }
  }

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {
  }
}
