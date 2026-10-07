#include <jni.h>
#include <cmath>
#include <algorithm>

namespace {
struct Aabb{float minx,miny,minz,maxx,maxy,maxz;};
static bool overlap(const Aabb&a,const Aabb&b){
    return a.minx<=b.maxx&&a.maxx>=b.minx&&a.miny<=b.maxy&&a.maxy>=b.miny&&a.minz<=b.maxz&&a.maxz>=b.minz;
}
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_noir_game_engine_NoirNative_fixedStepAlpha(JNIEnv*,jclass,jfloat accumulator,jfloat fixedDelta){
    if(fixedDelta<=0)return 0;
    float a=std::max(0.0f,std::min(accumulator,fixedDelta));
    return a/fixedDelta;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_noir_game_engine_NoirNative_countAabbPairs(JNIEnv* env,jclass,jfloatArray boxes){
    if(!boxes)return 0;
    const jsize n=env->GetArrayLength(boxes);
    if(n<6||n%6!=0)return 0;
    jfloat*data=env->GetFloatArrayElements(boxes,nullptr);
    const int count=n/6;
    int pairs=0;
    for(int i=0;i<count;i++){
        Aabb a{data[i*6],data[i*6+1],data[i*6+2],data[i*6+3],data[i*6+4],data[i*6+5]};
        for(int j=i+1;j<count;j++){
            Aabb b{data[j*6],data[j*6+1],data[j*6+2],data[j*6+3],data[j*6+4],data[j*6+5]};
            if(overlap(a,b))++pairs;
        }
    }
    env->ReleaseFloatArrayElements(boxes,data,JNI_ABORT);
    return pairs;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_noir_game_engine_NoirNative_smoothDamp(JNIEnv*,jclass,jfloat current,jfloat target,jfloat currentVelocity,jfloat smoothTime,jfloat maxSpeed,jfloat dt){
    float h=std::max(0.0f,std::min(dt,0.05f));
    float st=std::max(0.0001f,smoothTime);
    float omega=2.0f/st;
    float x=omega*h;
    float exp=1.0f/(1.0f+x+0.48f*x*x+0.235f*x*x*x);
    float change=current-target;
    float maxChange=std::max(0.0f,maxSpeed)*st;
    change=std::max(-maxChange,std::min(change,maxChange));
    float temp=(currentVelocity+omega*change)*h;
    float newVelocity=(currentVelocity-omega*temp)*exp;
    float output=target+(change+temp)*exp;
    if((target-current>0)==(output>target)){
        output=target;
        newVelocity=(output-target)/h;
    }
    return output;
}
