package com.nartgnud.core.utils

open class PlayMp3Exception(message: String): Exception(message){
    class NetworkFailureException(message: String) : PlayMp3Exception(message)
}