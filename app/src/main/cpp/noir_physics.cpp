#include <jni.h>
#include <cmath>
#include <algorithm>

namespace {
struct Vec3 { float x,y,z; };

static Vec3 read3(const jfloat* v) { return {v[0],v[1],v[2]}; }
static void write3(jfloat* v,const Vec3& p){v[0]=p.x;v[1]=p.y;v[2]=p.z;}
static float dot(const Vec3&a,const Vec3&b){return a.x*b.x+a.y*b.y+a.z*b.z;}
static Vec3 sub(const Vec3&a,const Vec3&b){return {a.x-b.x,a.y-b.y,a.z-b.z};}
}

extern "C" JNIEXPORT void JNICALL
Java_com_noir_game_engine_NoirNative_stepRigidBody(JNIEnv* env,jclass,jfloatArray state,jfloat dt,jfloat gravity){
    if(!state || env->GetArrayLength(state)<6)return;
    jfloat* s=env->GetFloatArrayElements(state,nullptr);
    float h=std::max(0.0f,std::min(dt,0.05f));
    s[4]+=gravity*h;
    s[0]+=s[3]*h;
    s[1]+=s[4]*h;
    s[2]+=s[5]*h;
    if(s[1]<0.0f){s[1]=0.0f;s[4]=0.0f;}
    env->ReleaseFloatArrayElements(state,s,0);
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_noir_game_engine_NoirNative_raySphereHit(JNIEnv* env,jclass,jfloatArray origin,jfloatArray dir,jfloatArray center,jfloat radius){
    if(!origin||!dir||!center||env->GetArrayLength(origin)<3||env->GetArrayLength(dir)<3||env->GetArrayLength(center)<3)return -1.0f;
    jfloat* o=env->GetFloatArrayElements(origin,nullptr);
    jfloat* d=env->GetFloatArrayElements(dir,nullptr);
    jfloat* c=env->GetFloatArrayElements(center,nullptr);
    Vec3 O=read3(o),D=read3(d),C=read3(c);
    Vec3 oc=sub(O,C);
    float a=dot(D,D),b=2.0f*dot(oc,D),cc=dot(oc,oc)-radius*radius;
    float disc=b*b-4.0f*a*cc;
    float hit=-1.0f;
    if(a>1e-8f && disc>=0.0f){
        float root=std::sqrt(disc);
        float t0=(-b-root)/(2.0f*a),t1=(-b+root)/(2.0f*a);
        if(t0>=0.0f)hit=t0; else if(t1>=0.0f)hit=t1;
    }
    env->ReleaseFloatArrayElements(origin,o,JNI_ABORT);
    env->ReleaseFloatArrayElements(dir,d,JNI_ABORT);
    env->ReleaseFloatArrayElements(center,c,JNI_ABORT);
    return hit;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_noir_game_engine_NoirNative_mobilePbrShader(JNIEnv* env,jclass){
    static const char* shader =
        "#version 300 es\n"
        "precision highp float;\n"
        "in vec3 vNormal; in vec3 vPos; out vec4 frag;\n"
        "uniform vec3 uCamera,uSunDir,uSunColor,uBaseColor; uniform float uRough,uMetal;\n"
        "float sat(float x){return clamp(x,0.0,1.0);}\n"
        "vec3 fresnel(float c,vec3 f0){return f0+(1.0-f0)*pow(1.0-c,5.0);}\n"
        "void main(){vec3 N=normalize(vNormal),V=normalize(uCamera-vPos),L=normalize(-uSunDir);"
        "vec3 H=normalize(V+L);float NoL=sat(dot(N,L)),NoV=sat(dot(N,V)),NoH=sat(dot(N,H)),VoH=sat(dot(V,H));"
        "float a=max(0.045,uRough*uRough),a2=a*a;float d=NoH*NoH*(a2-1.0)+1.0;"
        "float D=a2/max(3.14159265*d*d,0.0001);float k=(a+1.0);k=k*k/8.0;"
        "float G=NoV/(NoV*(1.0-k)+k)*NoL/(NoL*(1.0-k)+k);"
        "vec3 F0=mix(vec3(0.04),uBaseColor,uMetal);vec3 F=fresnel(VoH,F0);"
        "vec3 spec=D*G*F/max(4.0*NoV*NoL,0.001);vec3 kd=(1.0-F)*(1.0-uMetal);"
        "vec3 color=(kd*uBaseColor/3.14159265+spec)*uSunColor*NoL;"
        "color=vec3(1.0)-exp(-color*1.15);color=pow(color,vec3(1.0/2.2));frag=vec4(color,1.0);}";
    return env->NewStringUTF(shader);
}


extern "C" JNIEXPORT void JNICALL
Java_com_noir_game_engine_NoirNative_stepRigidBodyAdvanced(JNIEnv* env,jclass,jfloatArray state,jfloat dt,jfloat gravity,jfloat damping,jfloat floorY){
    if(!state || env->GetArrayLength(state)<6)return;
    jfloat* s=env->GetFloatArrayElements(state,nullptr);
    float h=std::max(0.0f,std::min(dt,0.05f));
    float d=std::max(0.0f,std::min(damping,50.0f));
    s[4]+=gravity*h;
    float damp=std::exp(-d*h);
    s[3]*=damp;s[4]*=damp;s[5]*=damp;
    s[0]+=s[3]*h;s[1]+=s[4]*h;s[2]+=s[5]*h;
    if(s[1]<floorY){s[1]=floorY;s[4]=0.0f;}
    env->ReleaseFloatArrayElements(state,s,0);
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_noir_game_engine_NoirNative_rayAabbHit(JNIEnv* env,jclass,jfloatArray origin,jfloatArray dir,jfloatArray minv,jfloatArray maxv){
    if(!origin||!dir||!minv||!maxv||env->GetArrayLength(origin)<3||env->GetArrayLength(dir)<3||env->GetArrayLength(minv)<3||env->GetArrayLength(maxv)<3)return -1.0f;
    jfloat* o=env->GetFloatArrayElements(origin,nullptr);
    jfloat* d=env->GetFloatArrayElements(dir,nullptr);
    jfloat* mn=env->GetFloatArrayElements(minv,nullptr);
    jfloat* mx=env->GetFloatArrayElements(maxv,nullptr);
    float tmin=0.0f,tmax=1.0e30f;
    for(int i=0;i<3;i++){
        if(std::abs(d[i])<1.0e-7f){
            if(o[i]<mn[i]||o[i]>mx[i]){tmin=-1.0f;break;}
        }else{
            float a=(mn[i]-o[i])/d[i],b=(mx[i]-o[i])/d[i];
            if(a>b)std::swap(a,b);
            tmin=std::max(tmin,a);tmax=std::min(tmax,b);
            if(tmin>tmax){tmin=-1.0f;break;}
        }
    }
    env->ReleaseFloatArrayElements(origin,o,JNI_ABORT);
    env->ReleaseFloatArrayElements(dir,d,JNI_ABORT);
    env->ReleaseFloatArrayElements(minv,mn,JNI_ABORT);
    env->ReleaseFloatArrayElements(maxv,mx,JNI_ABORT);
    return tmin;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_noir_game_engine_NoirNative_springDamper(JNIEnv*,jclass,jfloat current,jfloat velocity,jfloat target,jfloat stiffness,jfloat damping,jfloat dt){
    float h=std::max(0.0f,std::min(dt,0.05f));
    float k=std::max(0.0f,stiffness),c=std::max(0.0f,damping);
    float acceleration=(target-current)*k-velocity*c;
    return current+velocity*h+0.5f*acceleration*h*h;
}
