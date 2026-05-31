#version 460 core

in vec2 fragTextureCoordinate;

out vec4 fragColor;

layout (std430, binding = 0) readonly restrict buffer startBuffer {
    uint[] cells;
};

uniform vec2 start;
uniform vec2 viewSize;
uniform int sizeBits;
uniform int mask;
uniform vec3 cellColor;
uniform vec3 backColor;

void main() {
    vec2 position = start + viewSize * fragTextureCoordinate;
    int x = int(floor(position.x));
    int y = int(floor(position.y));

    uint value = cells[((y & mask) >> 5) << sizeBits | x & mask];
    vec3 color = (value >> y & 1) == 0 ? backColor : cellColor;

    fragColor = vec4(color, 1.0);
}