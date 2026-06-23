#version 460 core

in vec2 fragTextureCoordinate;

out vec4 fragColor;

uniform isampler2D board;
uniform isampler2D changedMap;
uniform vec2 start;
uniform vec2 viewSize;
uniform int boardSize;
uniform vec3 cellColor;
uniform vec3 backColor;

void main() {
    vec2 position = start + viewSize * fragTextureCoordinate;
    int x = int(floor(position.x));
    int y = int(floor(position.y));

    int value = texture(board, position / boardSize).r;
    vec3 color = (value >> (y << 2 | x & 3) & 1) == 0 ? backColor : cellColor;

//    if (texture(changedMap, position / boardSize).x != 0) color = mix(vec3(1, 0, 0), color, 0.5);

    fragColor = vec4(color, 1.0);
}