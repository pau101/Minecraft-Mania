#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PuzzleSampler;

uniform vec2 InSize;

varying vec2 texCoord;
varying vec2 oneTexel;

void main() {
    int rows = 3;
    float cellSize = (InSize.y / rows);
    vec2 dim = vec2(ceil(InSize.x * rows / InSize.y), rows);
    vec2 offset = floor((InSize - dim * cellSize) * 0.5);
    vec2 pos = texCoord * InSize;
    vec2 fcell = (pos - offset) / cellSize;
    vec2 cell = floor(fcell);
    vec4 value = texture2D(PuzzleSampler, (cell + 0.5) / 256.0);
    vec2 c = floor(value.rg * 255.0);
    vec2 frac = fcell - cell;
    //gl_FragColor = vec4(value.a * (color + vec3(value.b) * 0.2), 1.0);
    float border = 0.0125;
    if (value.a > 0) {
        //vec2 stripe = floor(frac * 14 + 0.5);
        if (value.b > 0 &&
                //mod(stripe.x + stripe.y, 2) == 0 &&
                (frac.x < border || frac.x >= (1.0 - border) || frac.y < border || frac.y >= (1 - border))) {
            gl_FragColor = vec4(1.0);
        } else {
            vec3 color;
            if (c == cell) {
                color = texture2D(DiffuseSampler, texCoord).rgb;
            } else {
                vec2 pp = ((c + frac) * cellSize + offset);
                vec2 p = pp / InSize;
                if (p.x < 0 || p.x > 1 || p.y < 0 || p.x > 1) {
                    color = mix(vec3(0.95), vec3(0.85), floor(fract((floor(abs(pp.x)) + floor(pp.y)) / 33) + 0.5));
                } else {
                    color = texture2D(DiffuseSampler, p).rgb;
                }
            }
            if (value.b > 0.5) {
                color = mix(color, vec3(1.0), 0.33);
            }
            gl_FragColor = vec4(color, 1.0);
        }
    } else {
        gl_FragColor = vec4(0.02, 0.02, 0.02, 1.0);
    }
    //gl_FragColor = vec4(mod(c.r + c.g, 2));
    //gl_FragColor = texture2D(DiffuseSampler, texCoord);
}
