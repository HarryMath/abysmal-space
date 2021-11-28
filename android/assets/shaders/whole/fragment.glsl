#ifdef GL_ES
precision highp float;
#endif

uniform sampler2D sceneTex;// 0
uniform vec2 center;// Mouse position
uniform float zoom; // camera zoom
uniform float aspectRatio;
uniform float r;
uniform float angle;

varying vec2 v_texCoords;

void main() {
    float R = 7.0 * r;
    float color_scale = 1.0;
    // get pixel coordinates
    vec2 l_texCoords = v_texCoords;
    vec2 normalCoords = v_texCoords;
    normalCoords.y = center.y + (l_texCoords.y - center.y) * aspectRatio;
    //get distance from center
    float d = distance(normalCoords, center) * pow(zoom, 0.1);
    if (d <= R) {
        if (d > r) {
            float closePoint = R * pow((d - r) / (R - r), 0.3);
            float newDistance = (d + closePoint) / 2.0;
            vec2 centerVector = v_texCoords - center;
            l_texCoords = center + (centerVector / d * newDistance);
            if (d < R) {
                newDistance = distance(l_texCoords, center);
                float rotation = (8.0 + sin(angle) * 3.0) * pow((R - d) /  R, 7.0);
                float rot = atan(centerVector.y, centerVector.x) + rotation;
                l_texCoords = vec2(center.x + newDistance * cos(rot), center.y + newDistance * sin(rot));
                if (d < r * 3.0) {
                    color_scale = pow(r * 2.0 / (d - r), 0.45);
                    vec4 colorAdd = vec4(1.0, 0.07 * color_scale, 0.05 * color_scale, 0.0);
                    gl_FragColor = texture2D(sceneTex, l_texCoords) + colorAdd * (color_scale - 1.0) * 0.11;
                    return;
                }
            }
        } else if (d == r) {
            color_scale = 30.0;
        } else {
            color_scale = 0.0;
        }
    }
    gl_FragColor = texture2D(sceneTex, l_texCoords) * color_scale;
}