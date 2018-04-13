#  MediaPlayer+TextureView封装原生的视频播放器
## 1.gradle
### Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
### Step 2. Add the dependency
```
dependencies {
    compile 'com.github.xieguangwei:MyCustomMediaPlayer:1.0'
}
```
### 感兴趣的可以下载demo看下，[同款的基于ExoPlayer封装的播放器>>>](https://github.com/xieguangwei/MyCustomExoPlayer)