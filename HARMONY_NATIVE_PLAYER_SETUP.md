# Harmony Native Player Setup Guide

This document explains:

- NewPipeExtractor
- ExoPlayer
- RecyclerView UI
- Firebase metadata cache
- Room local cache
- Native PlayerService

The goal is to keep **NewPipe playback behavior** but use a **simple UI and Firebase data source**.

---

## 1. Final Architecture

```text
Firebase / API
      ↓
Repository
      ↓
Room Database (cache)
      ↓
RecyclerView UI
      ↓
NewPipeExtractor
      ↓
PlayerManager
      ↓
PlayerService
      ↓
ExoPlayer
```

---

## 2. Add NewPipeExtractor

Add dependency in `app/build.gradle`:

```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.TeamNewPipe.NewPipeExtractor:NewPipeExtractor:0.25.0"
}
```

Used classes:

```text
StreamInfo
YouTubeService
SearchExtractor
```

Example:

```java
StreamInfo info = StreamInfo.getInfo(service, url);
```

---

## 3. Create Project Structure

```text
app/
 ├─ ui/
 │   ├─ home/
 │   ├─ playlist/
 │   ├─ auth/
 │   └─ menu/
 │
 ├─ player/
 │   ├─ PlayerManager.java
 │   ├─ PlayerHolder.java
 │   ├─ service/
 │   ├─ ui/
 │   └─ playqueue/
 │
 ├─ extractor/
 ├─ repository/
 ├─ database/
 ├─ api/
 ├─ model/
 └─ util/
```

---

## 4. Video Model

Create:

```text
model/VideoItem.java
```

```java
public class VideoItem {

    public String videoId;
    public String title;
    public String uploader;
    public String thumbnailUrl;
    public long duration;

}
```

---

## 5. Firebase Metadata Structure

Firestore collection:

```text
songs
 └── videoId
      ├── title
      ├── uploader
      ├── thumbnailUrl
      ├── duration
      └── genre
```

Only store metadata, not stream URLs.

---

## 6. Room Database

Tables:

```text
videos
favorites
playlists
playlist_songs
history
```

Example entity:

```text
database/entity/VideoEntity.java
```

```java
@Entity
public class VideoEntity {

    @PrimaryKey
    public String videoId;

    public String title;
    public String thumbnailUrl;
    public long duration;

}
```

---

## 7. Repository Layer

```text
repository/VideoRepository.java
```

Example:

```java
public class VideoRepository {

    public List<VideoItem> loadVideos() {

        if (cacheExists()) {
            return database.videoDao().getVideos();
        }

        List<VideoItem> firebaseVideos = firebaseService.fetchVideos();

        database.videoDao().insert(firebaseVideos);

        return firebaseVideos;
    }

}
```

---

## 8. Home Screen (RecyclerView)

Create:

```text
ui/home/HomeFragment.java
ui/home/VideoAdapter.java
```

RecyclerView loads videos from database.

Item layout should contain:

- thumbnail
- title
- uploader
- duration
- favorite icon

---

## 9. RecyclerView Adapter

```java
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {

    List<VideoItem> items;

    @Override
    public void onBindViewHolder(VideoHolder holder, int position) {

        VideoItem item = items.get(position);

        holder.title.setText(item.title);

        holder.itemView.setOnClickListener(v -> {

            playVideo(item.videoId);

        });
    }
}
```

---

## 10. Video Playback (Using NewPipeExtractor)

```java
public void playVideo(String videoId) {

    String url = "https://www.youtube.com/watch?v=" + videoId;

    StreamInfo info = StreamInfo.getInfo(service, url);

    playerManager.playStream(info);

}
```

This matches **NewPipe playback exactly**.

---

## 11. Player Components

Required folders:

```text
player/
player/service/
player/ui/
player/playqueue/
```

Important classes:

```text
PlayerManager
PlayerService
MainPlayer
AudioPlayerUi
PlayQueueManager
```

Features:

- background playback
- screen-off playback
- notification controls

---

## 12. Playlist Screen

Create:

```text
ui/playlist/PlaylistFragment.java
```

RecyclerView shows saved songs.

Database table:

```text
playlist_songs
```

Features:

- play playlist
- remove song

---

## 13. Login System

Create:

```text
ui/auth/LoginActivity.java
```

Use Firebase Authentication.

Menu should contain only:

```text
Login
Logout
Playlist
```

---

## 14. Remove WebView Playback

Delete:

```text
WebView playback
YouTube embedded player
```

Use only:

```text
NewPipeExtractor + ExoPlayer
```

---

## 15. Player Behavior

Playback must work like NewPipe:

```text
Tap video
   ↓
Extractor loads stream
   ↓
PlayerService starts
   ↓
ExoPlayer plays
   ↓
User minimizes app
   ↓
Audio continues
   ↓
Screen off
   ↓
Playback continues
```

---

## 16. Final UI

App screens:

```text
Home
Playlist
Menu
```

Menu contains:

```text
Login
Logout
Playlist
```

---

## 17. Final Result

Harmony will have:

- native Android UI
- NewPipeExtractor streams
- ExoPlayer playback
- Firebase cached video list
- playlist system
- background playback
- screen-off playback
