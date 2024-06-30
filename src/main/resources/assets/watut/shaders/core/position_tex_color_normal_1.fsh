#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec2 texCoord0;
in float vertexDistance;
in vec4 vertexColor;
in vec4 normal;

out vec4 fragColor;

vec4 testMethod() {
    return vec4(1.0);
}

float test2() {
    return 0.0;
}

vec4 mainImage(vec2 fragCoord)
{
    float Pi = 6.28318530718; // Pi*2

    // GAUSSIAN BLUR SETTINGS {{{
    float Directions = 16.0; // BLUR DIRECTIONS (Default 16.0 - More is better but slower)
    float Quality = 8.0; // BLUR QUALITY (Default 4.0 - More is better but slower)
    float Size = 20.0; // BLUR SIZE (Radius)
    // GAUSSIAN BLUR SETTINGS }}}

    vec2 iResolution = vec2(1920, 1080);

    vec2 Radius = Size/iResolution.xy;

    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy;
    // Pixel colour
    //vec4 Color = texture(iChannel0, uv);
    vec4 Color = vec4(0.0, 0.0, 0.0, 0.0);

    // Blur calculations
    for( float d=0.0; d<Pi; d+=Pi/Directions)
    {
        for(float i=1.0/Quality; i<=1.0; i+=1.0/Quality)
        {
            //Color += texture( iChannel0, uv+vec2(cos(d),sin(d))*Radius*i);
        }
    }

    // Output to screen
    Color /= Quality * Directions + 1.0;
    //return Color;
    return vec4(0.0);
}

vec4 mainImageNew( vec4 fragColor, vec2 fragCoord )
{
    float Pi = 6.28318530718; // Pi*2

    // GAUSSIAN BLUR SETTINGS {{{
    float Directions = 16.0; // BLUR DIRECTIONS (Default 16.0 - More is better but slower)
    float Quality = 3.0; // BLUR QUALITY (Default 4.0 - More is better but slower)
    float Size = 8.0; // BLUR SIZE (Radius)
    // GAUSSIAN BLUR SETTINGS }}}

    vec2 iResolution = vec2(1920, 1080);

    vec2 Radius = Size/iResolution.xy;

    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy;
    // Pixel colour
    //vec4 Color = texture(iChannel0, uv);

    // Blur calculations
    for( float d=0.0; d<Pi; d+=Pi/Directions)
    {
        for(float i=1.0/Quality; i<=1.0; i+=1.0/Quality)
        {
            fragColor.rgb += vec3(0.1);//texture( iChannel0, uv+vec2(cos(d),sin(d))*Radius*i);
        }
    }

    // Output to screen
    fragColor /= Quality * Directions - 15.0;
    //fragColor =  Color;
    return fragColor;
}

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    /*color.r = 0;
    color.g = 0;
    color.b = 0;*/
    vec2 test = vec2(0.0);
    //vec4 testReturn = testMethod();
    float test2 = test2();

    float rand = rand(texCoord0);

    fragColor = color;//linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    //fragColor.w *= rand;
    //fragColor = testReturn;
    //vec4 test2 = mainImage(test);
    //fragColor = mainImageNew(fragColor, texCoord0);
}
