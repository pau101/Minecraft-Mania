#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

void main() {
    vec2 offset = vec2(oneTexel.x * 4, 0.0);
    float r = texture2D(DiffuseSampler, texCoord - offset).r;
    float g = texture2D(DiffuseSampler, texCoord).g;
    float b = texture2D(DiffuseSampler, texCoord + offset).b;
    gl_FragColor = vec4(r, g, b, 1.0);
}
