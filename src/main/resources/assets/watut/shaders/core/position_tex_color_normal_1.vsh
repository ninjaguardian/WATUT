#version 150

#moj_import <fog.glsl>
#moj_import <light.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out vec2 texCoord0;
out float vertexDistance;
out vec4 vertexColor;
out vec4 normal;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    /*vec3 Light0_Direction_static = vec3(1.0, 1.0, 1.0);
    vec3 Light1_Direction_static = vec3(1.0, 1.0, 1.0);*/
    vec3 Light0_Direction_static = vec3(0.51, 0.85, 0.09);
    vec3 Light1_Direction_static = vec3(-0.51, 0.61, 0.59);

    texCoord0 = UV0;
    vertexDistance = fog_distance(ModelViewMat, Position, FogShape);
    //vertexDistance = 100F;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
    //vertexColor = Color;
    vertexColor = minecraft_mix_light(Light0_Direction_static, Light1_Direction_static, Normal, Color);
}
