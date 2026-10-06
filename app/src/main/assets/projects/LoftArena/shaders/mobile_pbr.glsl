#version 300 es
precision highp float;
layout(location=0) out vec4 NoirColor;
uniform vec4 uAlbedo;
uniform float uMetallic;
uniform float uRoughness;
uniform float uAO;
in vec3 vNormal;
in vec2 vUV;
void main(){
    vec3 N=normalize(vNormal);
    vec3 L=normalize(vec3(0.35,0.85,0.25));
    float ndl=max(dot(N,L),0.0);
    float diffuse=0.12+ndl*0.88;
    float spec=pow(max(dot(reflect(-L,N),normalize(vec3(0.1,0.7,0.7))),0.0),mix(8.0,128.0,1.0-uRoughness));
    vec3 color=uAlbedo.rgb*diffuse*uAO + vec3(spec)*mix(0.04,1.0,uMetallic);
    NoirColor=vec4(color,uAlbedo.a);
}
