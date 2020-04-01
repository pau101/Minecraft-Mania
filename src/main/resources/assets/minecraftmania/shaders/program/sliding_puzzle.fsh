#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PuzzleSampler;

uniform vec2 InSize;

varying vec2 texCoord;
varying vec2 oneTexel;

void main() {
    int rows = 5;
    float cellSize = InSize.y / rows;
    vec2 dim = vec2(ceil(InSize.x * rows / InSize.y), rows);
    vec2 offset = (InSize - dim * cellSize) * 0.5;
    vec2 pos = texCoord * InSize;
    vec2 fcell = (pos - offset) / cellSize;
    vec2 cell = floor((pos - offset) / cellSize);
    vec4 value = texture2D(PuzzleSampler, (cell + 0.5) / 256.0);
    vec2 c = floor(value.rg * 255.0);
    vec3 color = texture2D(DiffuseSampler, ((c + (fcell - cell)) * cellSize + offset) / InSize).rgb;
    gl_FragColor = vec4(value.a * (color + vec3(value.b) * 0.2), 1.0);
    //gl_FragColor = vec4(mod(c.x + c.y, 2.0));
    //gl_FragColor = texture2D(DiffuseSampler, texCoord);
}
