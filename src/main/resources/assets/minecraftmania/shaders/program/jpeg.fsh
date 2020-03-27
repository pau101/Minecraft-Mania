#version 120

const float PI = 3.1415926535;
const int QUALITY = 10;
const int TERMS = 4;

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float Time;

varying vec2 texCoord;
varying vec2 oneTexel;

float m[64];

// https://unix4lyfe.org/dct/listing3.c
void dct(int p[64]) {
    int i;
    int rows[64];

    const int	c1   = 1004, /* cos(pi/16) << 10 */
                s1   =  200, /* sin(pi/16) */
                c3   =  851, /* cos(3pi/16) << 10 */
                s3   =  569, /* sin(3pi/16) << 10 */
                r2c6 =  554, /* sqrt(2)*cos(6pi/16) << 10 */
                r2s6 = 1337, /* sqrt(2)*sin(6pi/16) << 10 */
                r2   =  181; /* sqrt(2) << 7 */

    int x0,x1,x2,x3,x4,x5,x6,x7,x8;

    /* transform rows */
    for (i = 0; i < 8; i++) {
        x0 = p[0+8*i];
        x1 = p[1+8*i];
        x2 = p[2+8*i];
        x3 = p[3+8*i];
        x4 = p[4+8*i];
        x5 = p[5+8*i];
        x6 = p[6+8*i];
        x7 = p[7+8*i];

        /* Stage 1 */
        x8=x7+x0;
        x0-=x7;
        x7=x1+x6;
        x1-=x6;
        x6=x2+x5;
        x2-=x5;
        x5=x3+x4;
        x3-=x4;

        /* Stage 2 */
        x4=x8+x5;
        x8-=x5;
        x5=x7+x6;
        x7-=x6;
        x6=c1*(x1+x2);
        x2=(-s1-c1)*x2+x6;
        x1=(s1-c1)*x1+x6;
        x6=c3*(x0+x3);
        x3=(-s3-c3)*x3+x6;
        x0=(s3-c3)*x0+x6;

        /* Stage 3 */
        x6=x4+x5;
        x4-=x5;
        x5=r2c6*(x7+x8);
        x7=(-r2s6-r2c6)*x7+x5;
        x8=(r2s6-r2c6)*x8+x5;
        x5=x0+x2;
        x0-=x2;
        x2=x3+x1;
        x3-=x1;

        /* Stage 4 and output */
        rows[i*8+0]=x6;
        rows[i*8+4]=x4;
        rows[i*8+2]=x8 / 1024; // >> 10
        rows[i*8+6]=x7 / 1024;
        rows[i*8+7]=(x2-x5) / 1024;
        rows[i*8+1]=(x2+x5) / 1024;
        rows[i*8+3]=(x3*r2) / 131072; // >> 17
        rows[i*8+5]=(x0*r2) / 131072;
    }

    /* transform columns */
    for (i = 0; i < TERMS; i++) {
        x0 = rows[0*8+i];
        x1 = rows[1*8+i];
        x2 = rows[2*8+i];
        x3 = rows[3*8+i];
        x4 = rows[4*8+i];
        x5 = rows[5*8+i];
        x6 = rows[6*8+i];
        x7 = rows[7*8+i];

        /* Stage 1 */
        x8=x7+x0;
        x0-=x7;
        x7=x1+x6;
        x1-=x6;
        x6=x2+x5;
        x2-=x5;
        x5=x3+x4;
        x3-=x4;

        /* Stage 2 */
        x4=x8+x5;
        x8-=x5;
        x5=x7+x6;
        x7-=x6;
        x6=c1*(x1+x2);
        x2=(-s1-c1)*x2+x6;
        x1=(s1-c1)*x1+x6;
        x6=c3*(x0+x3);
        x3=(-s3-c3)*x3+x6;
        x0=(s3-c3)*x0+x6;

        /* Stage 3 */
        x6=x4+x5;
        x4-=x5;
        x5=r2c6*(x7+x8);
        x7=(-r2s6-r2c6)*x7+x5;
        x8=(r2s6-r2c6)*x8+x5;
        x5=x0+x2;
        x0-=x2;
        x2=x3+x1;
        x3-=x1;

        /* Stage 4 and output */
        m[0*8+i]=((x6+16) / 8); // >> 3
        m[4*8+i]=((x4+16) / 8);
        m[2*8+i]=((x8+16384) / 8192); // >> 13
        m[6*8+i]=((x7+16384) / 8192);
        m[7*8+i]=((x2-x5+16384) / 8192);
        m[1*8+i]=((x2+x5+16384) / 8192);
        m[3*8+i]=(((x3 / 256/* >> 8*/)*r2+8192) / 4096); // >> 12
        m[5*8+i]=(((x0 / 256)*r2+8192) / 4096);
    }
}

const int LUM_Q[64] = int[] (
    16, 11, 10, 16,  24,  40,  51,  61,
    12, 12, 14, 19,  26,  58,  60,  55,
    14, 13, 16, 24,  40,  57,  69,  56,
    14, 17, 22, 29,  51,  87,  80,  62,
    18, 22, 37, 56,  68, 109, 103,  77,
    24, 35, 55, 64,  81, 104, 113,  92,
    49, 64, 78, 87, 103, 121, 120, 101,
    72, 92, 95, 98, 112, 100, 103,  99
);

const int CHROMA_Q[64] = int[] (
    17, 18, 24, 47, 99, 99, 99, 99,
    18, 21, 26, 66, 99, 99, 99, 99,
    24, 26, 56, 99, 99, 99, 99, 99,
    47, 66, 99, 99, 99, 99, 99, 99,
    99, 99, 99, 99, 99, 99, 99, 99,
    99, 99, 99, 99, 99, 99, 99, 99,
    99, 99, 99, 99, 99, 99, 99, 99,
    99, 99, 99, 99, 99, 99, 99, 99
);

#define QUANTIZE(Q) { \
    for (int y = 0; y < TERMS; y++) { \
        for (int x = 0; x < TERMS; x++) { \
            int q = (50 + Q[y*8+x] * (QUALITY >= 50 ? 200 - 2 * QUALITY : 5000 / QUALITY)) / 100; \
            m[y*8+x] = floor(m[y*8+x] / q + 0.5) * q; \
        } \
    } \
}

float idct(vec2 pos) {
    float z = 0.0;
    for (int u = 0; u < TERMS; u++) {
        for (int v = 0; v < TERMS; v++) {
            float Cu = u == 0 ? 1.0 / sqrt(2.0) : 1.0;
            float Cv = v == 0 ? 1.0 / sqrt(2.0) : 1.0;
            float S = m[v*8+u];
            float q = Cu * Cv * S *
                cos((2*pos.x+1) * u * PI/16.0) *
                cos((2*pos.y+1) * v * PI/16.0);
            z += q;
        }
    }
    return clamp(z / (4.0 * 255.0), 0.0, 1.0);
}

// https://www.w3.org/Graphics/JPEG/jfif3.pdf pg. 3
vec3 rgb_to_ycbcr(vec3 rgb) {
    return vec3(
         0.299  * rgb.r + 0.587  * rgb.g + 0.114  * rgb.b,
        -0.1687 * rgb.r - 0.3313 * rgb.g + 0.5    * rgb.b + 0.5,
         0.5    * rgb.r - 0.4187 * rgb.g - 0.0813 * rgb.b + 0.5
    );
}

vec3 ycbcr_to_rgb(vec3 ycbcr) {
    return vec3(
        ycbcr.x                             + 1.402   * (ycbcr.z - 0.5),
        ycbcr.x - 0.34414 * (ycbcr.y - 0.5) - 0.71414 * (ycbcr.z - 0.5),
        ycbcr.x + 1.772   * (ycbcr.y - 0.5)
    );
}

int red[64];
int green[64];
int blue[64];

void main() {
    vec2 block = floor(texCoord * InSize / 8) * 8;
    vec2 pos = floor(mod(texCoord * InSize, 8));
    for (int x = 0; x < 8; x++) {
        for (int y = 0; y < 8; y++) {
            vec3 color = rgb_to_ycbcr(texture2D(DiffuseSampler, (block + vec2(x + 0.5, y + 0.5)) / InSize).rgb) * 255.0;
            red[x+y*8] = int(color.r);
            green[x+y*8] = int(color.g);
            blue[x+y*8] = int(color.b);
        }
    }
    dct(red);
    QUANTIZE(LUM_Q);
    float r = idct(pos);
    dct(green);
    QUANTIZE(CHROMA_Q);
    float g = idct(pos);
    dct(blue);
    QUANTIZE(CHROMA_Q);
    float b = idct(pos);
    gl_FragColor = vec4(ycbcr_to_rgb(vec3(r, g, b)), 1.0);
}
