package com.mikilangelo.abysmal.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.mikilangelo.abysmal.models.objectsData.DestroyableObjectData;
import com.mikilangelo.abysmal.models.objectsData.IdentityData;
import com.mikilangelo.abysmal.models.objectsData.LaserData;
import com.mikilangelo.abysmal.models.objectsData.ShieldData;
import com.mikilangelo.abysmal.tools.Geometry;

public class CollisionHandler implements ContactListener {

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
        System.out.println("ignored contact");
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
      else if (bodyA.getUserData() instanceof DestroyableObjectData) {
        final float speed = bodyB.getLinearVelocity().sub(bodyA.getLinearVelocity()).len();
        float damage = speed > 3 ? (float) (Math.pow(bodyB.getMass(), 0.3) *
                Math.pow(bodyB.getLinearVelocity()
                        .sub(bodyA.getLinearVelocity().scl(0.5f))
                        .len(), 1.71)) / 17f : 0;
        if (bodyBData instanceof LaserData) {
          damage = (((LaserData) bodyBData).damage * 5 + damage) / 5.5f;
        }
        // System.out.println("\nimpulse: " + bodyB.getMass() * bodyB.getLinearVelocity().len());
        // System.out.println("damage: " + damage);
        ((DestroyableObjectData) bodyA.getUserData()).damage(damage);
        if (((DestroyableObjectData) bodyA.getUserData()).getHealth() < 0) {
          contact.setEnabled(false);
        }
      }
      else if (bodyAData instanceof ShieldData) {
        ((ShieldData) bodyAData).lastTouches.add(
                Geometry.defineAngle(contact.getWorldManifold().getPoints()[0], bodyA.getPosition())
                * MathUtils.radiansToDegrees
        );
      }
    }
  }

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {
  }
}
