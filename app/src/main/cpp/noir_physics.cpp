#include <jni.h>
#include <cmath>
#include <algorithm>

namespace {
struct Vec3{float x,y,z;};
static Vec3 read3(const jfloat* v){return{v[0],v[1],v[2]};}
static float dot(const Vec3&a,const Vec3&b){return a.x*b.x+a.y*b.y+a.z*b.z;}
static Vec3 add(const Vec3&a,const Vec3&b){return{a.x+b.x,a.y+b.y,a.z+b.z};}
static Vec3 sub(const Vec3&a,const Vec3&b){return{a.x-b.x,a.y-b.y,a.z-b.z};}
static float clampdt(float dt){return std::max(0.0f,std::min(dt,0.05f));}
static float rayAabb(const Vec3&o,const Vec3&d,const Vec3&mn,const Vec3&mx){
    float tmin=0.0f,tmax=1.0e30f;
    const float ro[3]={o.x,o.y,o.z},rd[3]={d.x,d.y,d.z},lo[3]={mn.x,mn.y,mn.z},hi[3]={mx.x,mx.y,mx.z};
    for(int i=0;i<3;i++){
        if(std::abs(rd[i])<1e-7f){if(ro[i]<lo[i]||ro[i]>hi[i])return-1.0f;}
        else{float a=(lo[i]-ro[i])/rd[i],b=(hi[i]-ro[i])/rd[i];if(a>b)std::swap(a,b);tmin=std::max(tmin,a);tmax=std::min(tmax,b);if(tmin>tmax)return-1.0f;}
    }
    return tmin;
}
}

extern "C" JNIEXPORT void JNICALL Java_com_noir_game_engine_NoirNative_stepRigidBody(JNIEnv*e,jclass,jfloatArray state,jfloat dt,jfloat gravity){
    if(!state||e->GetArrayLength(state)<6)return;jfloat*s=e->GetFloatArrayElements(state,nullptr);float h=clampdt(dt);
    s[4]+=gravity*h;s[0]+=s[3]*h;s[1]+=s[4]*h;s[2]+=s[5]*h;if(s[1]<0){s[1]=0;s[4]=0;}e->ReleaseFloatArrayElements(state,s,0);
}
extern "C" JNIEXPORT void JNICALL Java_com_noir_game_engine_NoirNative_stepRigidBodyAdvanced(JNIEnv*e,jclass,jfloatArray state,jfloat dt,jfloat gravity,jfloat damping,jfloat floorY){
    if(!state||e->GetArrayLength(state)<6)return;jfloat*s=e->GetFloatArrayElements(state,nullptr);float h=clampdt(dt),d=std::max(0.f,std::min(damping,50.f));
    s[4]+=gravity*h;float damp=std::exp(-d*h);s[3]*=damp;s[4]*=damp;s[5]*=damp;s[0]+=s[3]*h;s[1]+=s[4]*h;s[2]+=s[5]*h;if(s[1]<floorY){s[1]=floorY;if(s[4]<0)s[4]=0;}e->ReleaseFloatArrayElements(state,s,0);
}
extern "C" JNIEXPORT void JNICALL Java_com_noir_game_engine_NoirNative_stepRigidBodyContact(JNIEnv*e,jclass,jfloatArray state,jfloat dt,jfloat gravity,jfloat damping,jfloat floorY,jfloat restitution,jfloat friction){
    if(!state||e->GetArrayLength(state)<6)return;jfloat*s=e->GetFloatArrayElements(state,nullptr);float h=clampdt(dt),d=std::max(0.f,std::min(damping,100.f)),r=std::max(0.f,std::min(restitution,1.f)),f=std::max(0.f,std::min(friction,20.f));
    s[4]+=gravity*h;float damp=std::exp(-d*h);s[3]*=damp;s[4]*=damp;s[5]*=damp;s[0]+=s[3]*h;s[1]+=s[4]*h;s[2]+=s[5]*h;
    if(s[1]<floorY){s[1]=floorY;if(s[4]<0)s[4]=-s[4]*r;float gd=std::exp(-f*h);s[3]*=gd;s[5]*=gd;if(std::abs(s[4])<.05f)s[4]=0;}
    e->ReleaseFloatArrayElements(state,s,0);
}
extern "C" JNIEXPORT jfloat JNICALL Java_com_noir_game_engine_NoirNative_raySphereHit(JNIEnv*e,jclass,jfloatArray origin,jfloatArray dir,jfloatArray center,jfloat radius){
    if(!origin||!dir||!center||e->GetArrayLength(origin)<3||e->GetArrayLength(dir)<3||e->GetArrayLength(center)<3)return-1;
    jfloat*o=e->GetFloatArrayElements(origin,nullptr),*d=e->GetFloatArrayElements(dir,nullptr),*c=e->GetFloatArrayElements(center,nullptr);
    Vec3 O=read3(o),D=read3(d),C=read3(c),oc=sub(O,C);float a=dot(D,D),b=2*dot(oc,D),cc=dot(oc,oc)-radius*radius,disc=b*b-4*a*cc,hit=-1;
    if(a>1e-8f&&disc>=0){float q=std::sqrt(disc),t0=(-b-q)/(2*a),t1=(-b+q)/(2*a);if(t0>=0)hit=t0;else if(t1>=0)hit=t1;}
    e->ReleaseFloatArrayElements(origin,o,JNI_ABORT);e->ReleaseFloatArrayElements(dir,d,JNI_ABORT);e->ReleaseFloatArrayElements(center,c,JNI_ABORT);return hit;
}
extern "C" JNIEXPORT jfloat JNICALL Java_com_noir_game_engine_NoirNative_rayAabbHit(JNIEnv*e,jclass,jfloatArray origin,jfloatArray dir,jfloatArray minv,jfloatArray maxv){
    if(!origin||!dir||!minv||!maxv||e->GetArrayLength(origin)<3||e->GetArrayLength(dir)<3||e->GetArrayLength(minv)<3||e->GetArrayLength(maxv)<3)return-1;
    jfloat*o=e->GetFloatArrayElements(origin,nullptr),*d=e->GetFloatArrayElements(dir,nullptr),*mn=e->GetFloatArrayElements(minv,nullptr),*mx=e->GetFloatArrayElements(maxv,nullptr);
    float t=rayAabb(read3(o),read3(d),read3(mn),read3(mx));e->ReleaseFloatArrayElements(origin,o,JNI_ABORT);e->ReleaseFloatArrayElements(dir,d,JNI_ABORT);e->ReleaseFloatArrayElements(minv,mn,JNI_ABORT);e->ReleaseFloatArrayElements(maxv,mx,JNI_ABORT);return t;
}
extern "C" JNIEXPORT jfloat JNICALL Java_com_noir_game_engine_NoirNative_sweepSphereAabb(JNIEnv*e,jclass,jfloatArray origin,jfloat radius,jfloatArray direction,jfloat maxDistance,jfloatArray minv,jfloatArray maxv){
    if(!origin||!direction||!minv||!maxv)return-1;jfloat*o=e->GetFloatArrayElements(origin,nullptr),*d=e->GetFloatArrayElements(direction,nullptr),*mn=e->GetFloatArrayElements(minv,nullptr),*mx=e->GetFloatArrayElements(maxv,nullptr);
    Vec3 R{radius,radius,radius};float t=rayAabb(read3(o),read3(d),sub(read3(mn),R),add(read3(mx),R));if(t>maxDistance)t=-1;
    e->ReleaseFloatArrayElements(origin,o,JNI_ABORT);e->ReleaseFloatArrayElements(direction,d,JNI_ABORT);e->ReleaseFloatArrayElements(minv,mn,JNI_ABORT);e->ReleaseFloatArrayElements(maxv,mx,JNI_ABORT);return t;
}
extern "C" JNIEXPORT jboolean JNICALL Java_com_noir_game_engine_NoirNative_sphereAabbOverlap(JNIEnv*e,jclass,jfloatArray center,jfloat radius,jfloatArray minv,jfloatArray maxv){
    if(!center||!minv||!maxv)return JNI_FALSE;jfloat*c=e->GetFloatArrayElements(center,nullptr),*mn=e->GetFloatArrayElements(minv,nullptr),*mx=e->GetFloatArrayElements(maxv,nullptr);
    Vec3 C=read3(c),MN=read3(mn),MX=read3(mx);float dx=std::max(MN.x-C.x,0.f)+std::min(MX.x-C.x,0.f),dy=std::max(MN.y-C.y,0.f)+std::min(MX.y-C.y,0.f),dz=std::max(MN.z-C.z,0.f)+std::min(MX.z-C.z,0.f);
    bool hit=dx*dx+dy*dy+dz*dz<=radius*radius;e->ReleaseFloatArrayElements(center,c,JNI_ABORT);e->ReleaseFloatArrayElements(minv,mn,JNI_ABORT);e->ReleaseFloatArrayElements(maxv,mx,JNI_ABORT);return hit?JNI_TRUE:JNI_FALSE;
}
extern "C" JNIEXPORT jfloat JNICALL Java_com_noir_game_engine_NoirNative_springDamper(JNIEnv*,jclass,jfloat current,jfloat velocity,jfloat target,jfloat stiffness,jfloat damping,jfloat dt){
    float h=clampdt(dt),k=std::max(0.f,stiffness),c=std::max(0.f,damping);return current+velocity*h+.5f*((target-current)*k-velocity*c)*h*h;
}
extern "C" JNIEXPORT jstring JNICALL Java_com_noir_game_engine_NoirNative_mobilePbrShader(JNIEnv*e,jclass){
    static const char*shader="#version 300 es\nprecision highp float;in vec3 vNormal;in vec3 vPos;out vec4 frag;uniform vec3 uCamera,uSunDir,uSunColor,uBaseColor;uniform float uRough,uMetal;float sat(float x){return clamp(x,0.,1.);}vec3 F(float c,vec3 f0){return f0+(1.-f0)*pow(1.-c,5.);}void main(){vec3 N=normalize(vNormal),V=normalize(uCamera-vPos),L=normalize(-uSunDir),H=normalize(V+L);float nl=sat(dot(N,L)),nv=sat(dot(N,V)),nh=sat(dot(N,H)),vh=sat(dot(V,H));float a=max(.045,uRough*uRough),a2=a*a,d=nh*nh*(a2-1.)+1.,D=a2/max(3.14159265*d*d,.0001),k=(a+1.)*(a+1.)/8.,G=nv/(nv*(1.-k)+k)*nl/(nl*(1.-k)+k);vec3 f0=mix(vec3(.04),uBaseColor,uMetal),f=F(vh,f0),spec=D*G*f/max(4.*nv*nl,.001),kd=(1.-f)*(1.-uMetal),c=(kd*uBaseColor/3.14159265+spec)*uSunColor*nl;c=vec3(1.)-exp(-c*1.15);c=pow(c,vec3(1./2.2));frag=vec4(c,1.);}";
    return e->NewStringUTF(shader);
}
