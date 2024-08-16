#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES vTexture;
void main() {
    vec4 color = texture2D(vTexture, textureCoordinate);
    if(textureCoordinate.y < 0.5) {
    gl_FragColor = vec4(color.r,0.0,0.0,0.2);
    } else {
    gl_FragColor = vec4(color);
    }
}