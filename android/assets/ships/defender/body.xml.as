package
{
	import Box2D.Dynamics.*;
	import Box2D.Collision.*;
	import Box2D.Collision.Shapes.*;
	import Box2D.Common.Math.*;
    import flash.utils.Dictionary;

    public class PhysicsData extends Object
	{
		// ptm ratio
        public var ptm_ratio:Number = 32;
		
		// the physcis data 
		var dict:Dictionary;
		
        //
        // bodytype:
        //  b2_staticBody
        //  b2_kinematicBody
        //  b2_dynamicBody

        public function createBody(name:String, world:b2World, bodyType:uint, userData:*):b2Body
        {
            var fixtures:Array = dict[name];

            var body:b2Body;
            var f:Number;

            // prepare body def
            var bodyDef:b2BodyDef = new b2BodyDef();
            bodyDef.type = bodyType;
            bodyDef.userData = userData;

            // create the body
            body = world.CreateBody(bodyDef);

            // prepare fixtures
            for(f=0; f<fixtures.length; f++)
            {
                var fixture:Array = fixtures[f];

                var fixtureDef:b2FixtureDef = new b2FixtureDef();

                fixtureDef.density=fixture[0];
                fixtureDef.friction=fixture[1];
                fixtureDef.restitution=fixture[2];

                fixtureDef.filter.categoryBits = fixture[3];
                fixtureDef.filter.maskBits = fixture[4];
                fixtureDef.filter.groupIndex = fixture[5];
                fixtureDef.isSensor = fixture[6];

                if(fixture[7] == "POLYGON")
                {                    
                    var p:Number;
                    var polygons:Array = fixture[8];
                    for(p=0; p<polygons.length; p++)
                    {
                        var polygonShape:b2PolygonShape = new b2PolygonShape();
                        polygonShape.SetAsArray(polygons[p], polygons[p].length);
                        fixtureDef.shape=polygonShape;

                        body.CreateFixture(fixtureDef);
                    }
                }
                else if(fixture[7] == "CIRCLE")
                {
                    var circleShape:b2CircleShape = new b2CircleShape(fixture[9]);                    
                    circleShape.SetLocalPosition(fixture[8]);
                    fixtureDef.shape=circleShape;
                    body.CreateFixture(fixtureDef);                    
                }                
            }

            return body;
        }

		
        public function PhysicsData(): void
		{
			dict = new Dictionary();
			

			dict["ship"] = [

										[
											// density, friction, restitution
                                            2, 0, 0,
                                            // categoryBits, maskBits, groupIndex, isSensor
											1, 65535, 0, false,
											'POLYGON',

                                            // vertexes of decomposed polygons
                                            [

                                                [   new b2Vec2(1.7021276950836182/ptm_ratio, 64.0851058959961/ptm_ratio)  ,  new b2Vec2(9/ptm_ratio, 63/ptm_ratio)  ,  new b2Vec2(16/ptm_ratio, 76/ptm_ratio)  ,  new b2Vec2(1/ptm_ratio, 76/ptm_ratio)  ] ,
                                                [   new b2Vec2(15/ptm_ratio, 30/ptm_ratio)  ,  new b2Vec2(9/ptm_ratio, 43/ptm_ratio)  ,  new b2Vec2(1.4893616437911987/ptm_ratio, 40.68085479736328/ptm_ratio)  ,  new b2Vec2(1/ptm_ratio, 30/ptm_ratio)  ] ,
                                                [   new b2Vec2(9/ptm_ratio, 43/ptm_ratio)  ,  new b2Vec2(15/ptm_ratio, 30/ptm_ratio)  ,  new b2Vec2(39.787235260009766/ptm_ratio, 12.595741271972656/ptm_ratio)  ,  new b2Vec2(41/ptm_ratio, 93/ptm_ratio)  ,  new b2Vec2(16/ptm_ratio, 76/ptm_ratio)  ,  new b2Vec2(9/ptm_ratio, 63/ptm_ratio)  ] ,
                                                [   new b2Vec2(103/ptm_ratio, 58/ptm_ratio)  ,  new b2Vec2(51/ptm_ratio, 63/ptm_ratio)  ,  new b2Vec2(51.27659606933594/ptm_ratio, 43.23404312133789/ptm_ratio)  ,  new b2Vec2(103.61701965332031/ptm_ratio, 48.12765884399414/ptm_ratio)  ] ,
                                                [   new b2Vec2(51/ptm_ratio, 63/ptm_ratio)  ,  new b2Vec2(41/ptm_ratio, 93/ptm_ratio)  ,  new b2Vec2(39.787235260009766/ptm_ratio, 12.595741271972656/ptm_ratio)  ,  new b2Vec2(51.27659606933594/ptm_ratio, 43.23404312133789/ptm_ratio)  ] ,
                                                [   new b2Vec2(86/ptm_ratio, 102/ptm_ratio)  ,  new b2Vec2(21/ptm_ratio, 105/ptm_ratio)  ,  new b2Vec2(41/ptm_ratio, 93/ptm_ratio)  ,  new b2Vec2(85/ptm_ratio, 97/ptm_ratio)  ] ,
                                                [   new b2Vec2(16/ptm_ratio, 76/ptm_ratio)  ,  new b2Vec2(41/ptm_ratio, 93/ptm_ratio)  ,  new b2Vec2(21/ptm_ratio, 105/ptm_ratio)  ] ,
                                                [   new b2Vec2(21/ptm_ratio, 1/ptm_ratio)  ,  new b2Vec2(39.787235260009766/ptm_ratio, 12.595741271972656/ptm_ratio)  ,  new b2Vec2(15/ptm_ratio, 30/ptm_ratio)  ] ,
                                                [   new b2Vec2(85/ptm_ratio, 4/ptm_ratio)  ,  new b2Vec2(85.31914520263672/ptm_ratio, 7.702125549316406/ptm_ratio)  ,  new b2Vec2(39.787235260009766/ptm_ratio, 12.595741271972656/ptm_ratio)  ,  new b2Vec2(21/ptm_ratio, 1/ptm_ratio)  ]
											]

										]

									];

		}
	}
}
