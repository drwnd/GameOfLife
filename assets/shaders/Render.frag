#version 460 core

in vec2 fragTextureCoordinate;

out vec4 fragColor;

uniform isampler2D board;
uniform vec2 start;
uniform vec2 viewSize;
uniform int boardSize;
uniform vec3 cellColor;
uniform vec3 backColor;

void main() {
    vec2 position = start + viewSize * fragTextureCoordinate;
    int x = int(position.x);

    int value = texture(board, position / boardSize).r;
    vec3 color = (value >> x & 1) == 0 ? backColor : cellColor;

    fragColor = vec4(color, 1.0);
}