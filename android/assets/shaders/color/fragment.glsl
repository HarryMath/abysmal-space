#ifdef GL_ES
precision highp float;
#endif

uniform sampler2D sceneTex;// 0
uniform vec3 color;

varying vec2 v_texCoords;

void main() {
    vec4 init_color = texture2D(sceneTex, v_texCoords);
    float new_color_brightness = (color[0] + color[1] + color[2]) * 0.333;
    float brightnes = (init_color[0] + init_color[1] + init_color[2]) * 0.333;
    vec4 final_color = vec4(color[0], color[1], color[2], 1);
    if (brightnes <= new_color_brightness) {
        gl_FragColor = final_color * brightnes / new_color_brightness;
    } else {
        gl_FragColor = final_color * (1 - brightnes) + vec4(1, 1, 1, 1) * brightnes;
    }
}