#include <jni.h>
#include <iostream>
#include <cmath>
#include <vector>
#include <random>
#include <math.h>
#include "custom_Pack_Particle_Particle.h"
#include "custom_Pack_ParticleSystem.h"

using namespace std;

float K = 200.0f;

const double PI = 3.14159265358979323846;

double randomDouble(double a, double b) {
    // Create a random number generator
    random_device rd;  // Initialize random device
    mt19937 generator(rd());  // Use the random device to seed the generator
    uniform_real_distribution<double> distribution(a, b);  // Define the range

    return distribution(generator);  // Generate a random double in the range [a, b]
}

extern "C" {


vector<float> getCppVectorFromJavaVector(JNIEnv* env, jobject javaVector) {
    std::vector<float> cppVector;

    // Find the Java Vector class and required methods
    jclass vectorClass = env->FindClass("java/util/Vector");
    jmethodID sizeMethod = env->GetMethodID(vectorClass, "size", "()I");
    jmethodID getMethod = env->GetMethodID(vectorClass, "get", "(I)Ljava/lang/Object;");

    // Get the size of the Java Vector
    jint vectorSize = env->CallIntMethod(javaVector, sizeMethod);

    // Iterate through the Java Vector and extract each float element
    for (int i = 0; i < vectorSize; ++i) {
        jobject floatObj = env->CallObjectMethod(javaVector, getMethod, i);

        // Cast the Object to Float and get the float value
        jclass floatClass = env->FindClass("java/lang/Float");
        jmethodID floatValueMethod = env->GetMethodID(floatClass, "floatValue", "()F");
        jfloat value = env->CallFloatMethod(floatObj, floatValueMethod);

        // Add the float value to the C++ vector
        cppVector.push_back(value);

        // Clean up local references
        env->DeleteLocalRef(floatObj);
    }
  
    // Return the populated C++ vector
    return cppVector;
}



jobject getJavaVectorFromCppVector(JNIEnv* env, const std::vector<float>& cppVector) {
    // Find the Java Vector and Float classes
    jclass vectorClass = env->FindClass("java/util/Vector");
    jclass floatClass = env->FindClass("java/lang/Float");

    // Get the constructors and method IDs
    jmethodID vectorConstructor = env->GetMethodID(vectorClass, "<init>", "()V");
    jmethodID addMethod = env->GetMethodID(vectorClass, "add", "(Ljava/lang/Object;)Z");
    jmethodID floatConstructor = env->GetMethodID(floatClass, "<init>", "(F)V");

    // Create a new Java Vector instance
    jobject javaVector = env->NewObject(vectorClass, vectorConstructor);

    // Populate the Java Vector with Float objects
    for (float value : cppVector) {
        // Create a new Float object for each value in the C++ vector
        jobject floatObj = env->NewObject(floatClass, floatConstructor, value);
        
        // Add the Float object to the Java Vector
        env->CallBooleanMethod(javaVector, addMethod, floatObj);

        // Clean up the local reference to the Float object
        env->DeleteLocalRef(floatObj);
    }

    // Return the populated Java Vector
    return javaVector;
}


// Helper function to get a float value from Java Vector<Float>
float getVectorElement(JNIEnv* env, jobject vectorObj, int index) {
    jclass vectorClass = env->GetObjectClass(vectorObj);
    jmethodID getMethod = env->GetMethodID(vectorClass, "get", "(I)Ljava/lang/Object;");
    jobject floatObj = env->CallObjectMethod(vectorObj, getMethod, index);
    jclass floatClass = env->FindClass("java/lang/Float");
    jmethodID floatValueMethod = env->GetMethodID(floatClass, "floatValue", "()F");
    float value = env->CallFloatMethod(floatObj, floatValueMethod);
    env->DeleteLocalRef(floatObj);
    return value;
}

// Helper function to set values in Java Vector<Float>
void setVectorElement(JNIEnv* env, jobject vectorObj, int index, float value) {
    jclass vectorClass = env->GetObjectClass(vectorObj);
    jmethodID setMethod = env->GetMethodID(vectorClass, "set", "(ILjava/lang/Object;)Ljava/lang/Object;");
    jclass floatClass = env->FindClass("java/lang/Float");
    jobject floatObj = env->NewObject(floatClass, env->GetMethodID(floatClass, "<init>", "(F)V"), value);
    env->CallObjectMethod(vectorObj, setMethod, index, floatObj);
    env->DeleteLocalRef(floatObj);
}

// JNI implementation for update method
JNIEXPORT void JNICALL Java_custom_Pack_Particle_Particle_update(JNIEnv* env, jobject obj) {
    float dt = 1.0f;
    float maxVelocity = 5.0f;

    jclass particleClass = env->GetObjectClass(obj);

    // Retrieve mass
    jmethodID getMassMethod = env->GetMethodID(particleClass, "getMass", "()F");
    float mass = env->CallFloatMethod(obj, getMassMethod);

    // Retrieve force as Vector<Float>
    jmethodID getForceMethod = env->GetMethodID(particleClass, "getForce", "()Ljava/util/Vector;");
    jobject forceVector = env->CallObjectMethod(obj, getForceMethod);

    // Retrieve position as Vector<Float>
    jmethodID getPositionMethod = env->GetMethodID(particleClass, "getPosition", "()Ljava/util/Vector;");
    jobject positionVector = env->CallObjectMethod(obj, getPositionMethod);

    // Retrieve velocity as Vector<Float>
    jmethodID getVelocityMethod = env->GetMethodID(particleClass, "getVelocity", "()Ljava/util/Vector;");
    jobject velocityVector = env->CallObjectMethod(obj, getVelocityMethod);

    jmethodID setPositionMethod = env->GetMethodID(particleClass, "setPosition", "([F)V");
    jmethodID setVelocityMethod = env->GetMethodID(particleClass, "setVelocity", "([F)V");

    // Perform calculations
    float positionX = getVectorElement(env, positionVector, 0);
    float positionY = getVectorElement(env, positionVector, 1);
    float velocityX = getVectorElement(env, velocityVector, 0);
    float velocityY = getVectorElement(env, velocityVector, 1);
    float forceX = getVectorElement(env, forceVector, 0);
    float forceY = getVectorElement(env, forceVector, 1);

    // Update position based on force and velocity
    positionX += velocityX * dt + (forceX * dt * dt) / (2.0f * mass);
    positionY += velocityY * dt + (forceY * dt * dt) / (2.0f * mass);


    float finalPos[2] = { positionX, positionY };
    jfloatArray jPositionArray = env->NewFloatArray(2);
    env->SetFloatArrayRegion(jPositionArray, 0, 2, finalPos);
    env->CallVoidMethod(obj, setPositionMethod, jPositionArray);
    env->DeleteLocalRef(jPositionArray);

    velocityX += (forceX / mass) * dt;
    velocityY += (forceY / mass) * dt;


    float finalVel[2] = { velocityX, velocityY };
    jfloatArray jVelocityArray = env->NewFloatArray(2);
    env->SetFloatArrayRegion(jVelocityArray, 0, 2, finalVel);
    env->CallVoidMethod(obj, setVelocityMethod, jVelocityArray);
    env->DeleteLocalRef(jVelocityArray);

    // Clean up local references
    env->DeleteLocalRef(forceVector);
    env->DeleteLocalRef(positionVector);
    env->DeleteLocalRef(velocityVector);



}

JNIEXPORT void JNICALL Java_custom_Pack_ParticleSystem_setForces(JNIEnv* env, jobject obj) {
    jclass particleSystemClass = env->GetObjectClass(obj);

   
    jmethodID getParticlesMethod = env->GetMethodID(particleSystemClass, "getParticles", "()Ljava/util/Vector;");
    jmethodID getFieldPointsMethod = env->GetMethodID(particleSystemClass, "getFieldPoints", "()Ljava/util/Vector;");
    jmethodID isGravityEnabledMethod = env->GetMethodID(particleSystemClass, "isGravityEnabled", "()I");

    if(isGravityEnabledMethod == nullptr) cout<<"isGravityEnabledMethod is null"<<endl;
  
    jobject particles = env->CallObjectMethod(obj, getParticlesMethod);
    jobject fieldPoints = env->CallObjectMethod(obj, getFieldPointsMethod);
    jint gravityEnabled = env->CallIntMethod(obj, isGravityEnabledMethod);



    jclass vectorClass = env->FindClass("java/util/Vector");
    jmethodID vectorSizeMethod = env->GetMethodID(vectorClass, "size", "()I");
    jmethodID vectorGetMethod = env->GetMethodID(vectorClass, "get", "(I)Ljava/lang/Object;");

    jint numParticles = env->CallIntMethod(particles, vectorSizeMethod);
    jint numFieldPoints = env->CallIntMethod(fieldPoints, vectorSizeMethod);

    for (int i = 0; i < numParticles; i++) {
        jobject particle = env->CallObjectMethod(particles, vectorGetMethod, i);
        jclass particleClass = env->GetObjectClass(particle);

        jmethodID getPositionMethod = env->GetMethodID(particleClass, "getPosition", "()Ljava/util/Vector;");
        jmethodID getChargeMethod = env->GetMethodID(particleClass, "getCharge", "()F");
        jmethodID getMassMethod = env->GetMethodID(particleClass, "getMass", "()F");
        jmethodID setForceMethod = env->GetMethodID(particleClass, "setForce", "([F)V");

        jobject particlePosition = env->CallObjectMethod(particle, getPositionMethod);
        float charge = env->CallFloatMethod(particle, getChargeMethod);
        float mass = env->CallFloatMethod(particle, getMassMethod);

        float px = getVectorElement(env, particlePosition, 0);
        float py = getVectorElement(env, particlePosition, 1);

        float forceX = 0.0f;
        float forceY = 0.0f;
        if(gravityEnabled == 1)
            forceY = mass * 0.1;

        for (int j = 0; j < numFieldPoints; j++) {
            jobject fieldPoint = env->CallObjectMethod(fieldPoints, vectorGetMethod, j);
            jclass fieldPointClass = env->GetObjectClass(fieldPoint);

            jmethodID getFieldPointPosition = env->GetMethodID(fieldPointClass, "getPosition", "()Ljava/util/Vector;");
            jmethodID getFieldStrengthMethod = env->GetMethodID(fieldPointClass, "getFieldStrength", "()F");
            jmethodID getFieldTypeMethod = env->GetMethodID(fieldPointClass, "getType", "()Ljava/lang/String;");

            jobject fieldPointPosition = env->CallObjectMethod(fieldPoint, getFieldPointPosition);
            jstring javaString = (jstring) env->CallObjectMethod(fieldPoint, getFieldTypeMethod);
            const char* charString = env->GetStringUTFChars(javaString, nullptr);
            std::string fieldType(charString);
            

            float fieldStrength = env->CallFloatMethod(fieldPoint, getFieldStrengthMethod);

            float fx = getVectorElement(env, fieldPointPosition, 0);
            float fy = getVectorElement(env, fieldPointPosition, 1);

            float dx = fx - px;
            float dy = fy - py;
            float distanceSquared = dx * dx + dy * dy;
            float distance = sqrt(distanceSquared);

            float forceMagnitude;
            //cout<<fieldType<<endl;
            if(distance<10.0f) forceMagnitude = 0.0f;
            else
            {
                forceMagnitude = (charge * fieldStrength * K) / distanceSquared;
                if(fieldType == "B")
                {
                    forceMagnitude*=-1;
                }
                forceX += forceMagnitude * (dx / distance);
                forceY += forceMagnitude * (dy / distance);
            }

            env->ReleaseStringUTFChars(javaString, charString);

         
            env->DeleteLocalRef(javaString);

            env->DeleteLocalRef(fieldPointPosition);
            env->DeleteLocalRef(fieldPoint);
        }

        float force[2] = { forceX, forceY };
        jfloatArray jForceArray = env->NewFloatArray(2);
        env->SetFloatArrayRegion(jForceArray, 0, 2, force);

        env->CallVoidMethod(particle, setForceMethod, jForceArray);

        env->DeleteLocalRef(jForceArray);
        env->DeleteLocalRef(particlePosition);
        env->DeleteLocalRef(particle);
    }
    env->DeleteLocalRef(particles);
    env->DeleteLocalRef(fieldPoints);
}


JNIEXPORT jobjectArray JNICALL Java_custom_Pack_Emitter_Emitter_getVelocities(JNIEnv* env, jobject obj) {
    jclass emitterClass = env->GetObjectClass(obj);
    jmethodID getAngleMethod = env->GetMethodID(emitterClass, "getAngle", "()F");
    if (getAngleMethod == nullptr) {
        cerr << "Could not find getAngle() method" << endl;
        return nullptr;
    }

    float angle = env->CallFloatMethod(obj, getAngleMethod);
   

    jmethodID getSpeedMethod = env->GetMethodID(emitterClass, "getSpeed", "()F");
    if (getSpeedMethod == nullptr) {
        cerr << "Could not find getSpeed() method" << endl;
        return nullptr;
    }

    float speed = env->CallFloatMethod(obj, getSpeedMethod);
    

    jmethodID getSpreadMethod = env->GetMethodID(emitterClass, "getSpread", "()F");
    if (getSpreadMethod == nullptr) {
        cerr << "Could not find getSpread() method" << endl;
        return nullptr;
    }

    float spread = env->CallFloatMethod(obj, getSpreadMethod);


    vector<vector<float>> velocities;
            for (int i=0 ; i<10; i++) 
        {
            float angU = angle + spread/2;
            float angL = angle - spread/2;
            float ang = randomDouble(angL, angU);
            float vx = cos(ang) * speed;
            float vy = sin(ang) * speed;
            velocities.push_back({vx, vy});
        }




    // Determine the number of coordinates
    jsize numVelocities = velocities.size();

    // Create a 2D Java array (jobjectArray of jfloatArray)
    jobjectArray jVelocitiesArray = env->NewObjectArray(numVelocities, env->FindClass("[F"), nullptr);

    // Populate the jobjectArray with jfloatArrays
    for (jsize i = 0; i < numVelocities; ++i) {
        // Convert each C++ vector to a jfloatArray
        jfloatArray jInnerArray = env->NewFloatArray(2);
        env->SetFloatArrayRegion(jInnerArray, 0, 2, velocities[i].data());

        // Set the jfloatArray in the main jobjectArray
        env->SetObjectArrayElement(jVelocitiesArray, i, jInnerArray);

        // Delete the local reference to the inner array to prevent memory leaks
        env->DeleteLocalRef(jInnerArray);
    }

    // Return the 2D Java array
    return jVelocitiesArray;
}


JNIEXPORT void JNICALL Java_custom_Pack_Emitter_OscillatingEmitter_updateEmitter(JNIEnv* env, jobject obj) {
    jclass emitterClass = env->GetObjectClass(obj);
    jmethodID getAmplitudeMethod = env->GetMethodID(emitterClass, "getAmplitude", "()F");
    if (getAmplitudeMethod == nullptr) {
        cerr << "Could not find getMass() method" << endl;
        return;
    }

    float A = env->CallFloatMethod(obj, getAmplitudeMethod);
   


    jmethodID getFrequencyMethod = env->GetMethodID(emitterClass, "getFrequency", "()F");
    if (getFrequencyMethod == nullptr) {
        cerr << "Could not find getMass() method" << endl;
        return;
    }

    float f = env->CallFloatMethod(obj, getFrequencyMethod);
   
    jmethodID getThetaMethod = env->GetMethodID(emitterClass, "getTheta", "()F");
    if (getThetaMethod == nullptr) {
        cerr << "Could not find getMass() method" << endl;
        return;
    }

    float theta = env->CallFloatMethod(obj, getThetaMethod);


    // Get the getPosition() method ID
    jmethodID getPositionMethod = env->GetMethodID(emitterClass, "getPosition", "()Ljava/util/Vector;");
    if (getPositionMethod == nullptr) {
        std::cerr << "Could not find getPosition() method" << std::endl;
        return;
    }

    // Call getPosition() to get the Java Vector<Float>
    jobject positionVector = env->CallObjectMethod(obj, getPositionMethod);

    // Convert the Java Vector<Float> to a C++ std::vector<float>
    vector<float> cppPositionVector = getCppVectorFromJavaVector(env, positionVector);

    jmethodID getMeanPositionMethod = env->GetMethodID(emitterClass, "getMeanPosition", "()Ljava/util/Vector;");
    if (getMeanPositionMethod == nullptr) {
        std::cerr << "Could not find getMeanPosition() method" << std::endl;
        return;
    }

    // Call getPosition() to get the Java Vector<Float>
    jobject meanPositionVector = env->CallObjectMethod(obj, getMeanPositionMethod);

    // Convert the Java Vector<Float> to a C++ std::vector<float>
    vector<float> cppMeanPositionVector = getCppVectorFromJavaVector(env, meanPositionVector);


    float newTheta = theta + 2 * PI * f; // Increment theta by angular frequency
    float newY = cppMeanPositionVector[1] + A * sin(newTheta); // Update vertical position

    cppPositionVector[1] = newY; // Update the y-component of the position vector

    jmethodID setThetaMethod = env->GetMethodID(emitterClass, "setTheta", "(F)V");
    if (setThetaMethod == nullptr) {
        cerr << "Could not find getMass() method" << endl;
        return;
    }

    env->CallVoidMethod(obj, setThetaMethod, newTheta);


    // Convert the C++ vector to a Java Vector<Float>
    jobject javaPositionVector = getJavaVectorFromCppVector(env, cppPositionVector);

 
    jmethodID setPositionMethod = env->GetMethodID(emitterClass, "setPosition", "(Ljava/util/Vector;)V");

    if (setPositionMethod != nullptr) {
        env->CallVoidMethod(obj, setPositionMethod, javaPositionVector);
    } else {
        std::cerr << "Could not find setPosition() method" << std::endl;
    }

    
}
}