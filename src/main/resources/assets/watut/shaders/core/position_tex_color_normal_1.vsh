#version 150

#moj_import <fog.glsl>
#moj_import <light.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;
in vec3 SunDistNorm;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

uniform vec3 Light0_Direction2;
uniform vec3 Light1_Direction2;
uniform vec3 Lightning_Pos;
uniform vec4 CloudColor;
uniform vec3 VBO_Render_Pos;

out vec2 texCoord0;
out float vertexDistance;
out vec4 vertexColor;
out vec4 normal;

void main() {
    vec3 posAdj = Position + VBO_Render_Pos;
    gl_Position = ProjMat * ModelViewMat * vec4(posAdj, 1.0);

    //vec3 posFloored = floor(Position + 0.2);

    /*if (distance(posFloored, vec3(0, 200, 0)) > 50) {
        return;
    }*/

    /*if (distance(Position.y, 0) < 1) {
        return;
    }

    if (Position.y < 200) {
        return;
    }*/

    /*vec3 Light0_Direction_static = vec3(1.0, 1.0, 1.0);
    vec3 Light1_Direction_static = vec3(1.0, 1.0, 1.0);*/
    //vec3 Light0_Direction_static = vec3(0.51, 0.85, 0.09);
    //vec3 Light0_Direction_static = vec3(0, 1, 0);
    //vec3 Light1_Direction_static = vec3(-0.51, 0.61, 0.59);
    //vec3 Light1_Direction_static = vec3(0, 1, 0);

    texCoord0 = UV0;
    vertexDistance = fog_distance(ModelViewMat, posAdj, FogShape);
    //vertexDistance = 100F;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
    //minimize the impact normals have
    float normalImpact = 0.5;
    vec3 normal2 = (Normal * normalImpact) + vec3(normalImpact);
    //vec3 normal2 = (Normal * 1) + vec3(0);
    //vec3 normal2 = Normal;
    //vec3 normal2 = vec3(1, 1, 1);
    //normal2 = (vec3(1, 1, 1) * 0.9) + normal2;
    //vertexColor = Color;
    //float sunExposureImpact = 0.15;
    float sunExposureImpact = 0.15;
    //float impact2 = 1;
    float gradientAdj = SunDistNorm.y;
    //if (SunDistNorm.x == 0.9999) gradientAdj = 0;
    //if (SunDistNorm.x == 0) gradientAdj = 0;
    if (SunDistNorm.x == 1.0) gradientAdj = 0;
    //float blue = Color.b;
    vec4 test = CloudColor;
    //this is temporary so shader compiling doesnt remove "Color" data which breaks the shader lighting
    test.a = CloudColor.a + (Color.a * 0.0001) - 0.0001;
    vec4 newColor = test * ((1 - (SunDistNorm.x * sunExposureImpact)) + (gradientAdj * sunExposureImpact))/* * 3*/;
    //newColor.b = blue;
    /*newColor.r = max(Color.r, 0);
    newColor.g = max(Color.g, 0);
    newColor.b = max(Color.b, 0);*/

    /*newColor.r = max(Color.r, 1);
    newColor.g = max(Color.g, 1);
    newColor.b = max(Color.b, 1);*/

    //newColor.a = CloudColor.a;

    if (Lightning_Pos.y != -999) {
        if (distance(Lightning_Pos, posAdj) < 40) {
            float distFract = 1 - (distance(Lightning_Pos, posAdj) / 40);
            newColor.rgb *= 1 + distFract;
        }
    }
    vertexColor = minecraft_mix_light(Light0_Direction2, Light1_Direction2, normal2, newColor);
    vertexColor.a = CloudColor.a;
    //vertexColor = newColor;
    //vertexColor = minecraft_mix_light(Light0_Direction_static, Light1_Direction_static, normal2, newColor);
}
