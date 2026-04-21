# 🌊 WavePacket

**WavePacket** is an experimental project developed with **Kotlin Multiplatform (KMP)** that enables data communication via audio using the **AFSK (Audio Frequency Shift Keying)** technique.

With WavePacket, you can transform text messages into audio sine waves and decode them back on another device, functioning as a simple and efficient acoustic modem. 📻📱

---

## 🚀 Features

- **AFSK Encoding**: Converts text strings into audio sine waves.
- **Real-time Decoding**: Captures audio via microphone and processes bits to reconstruct the original message.
- **Multiplatform**: Native support for **Android** and **iOS** sharing the same business logic.
- **Sync Protocol**: Uses preambles and start/end frames to ensure message integrity.
- **Resilience**: Timeout mechanisms, Hysteresis (Schmitt Trigger), and noise filters to handle real-world acoustic environments.

---

## 🛠️ Technical Specifications

The project uses the following parameters for modulation:

| Parameter | Value | Description |
| :--- | :--- | :--- |
| **Sample Rate** | 44100 Hz | Standard audio quality (CD). |
| **Baud Rate** | 25 bps | Transmission speed (bits per second). |
| **Mark Freq** | 2400 Hz | Frequency representing **Bit 1** (Mark). |
| **Space Freq** | 5600 Hz | Frequency representing **Bit 0** (Space). |
| **Protocol** | Pure 8-bit | 8 data bits per block (no stop bits). |

---

## 📦 Protocol Structure

To ensure the receiver can identify the start and end of a transmission, messages follow this format:

1.  **Preamble (4x `0xAA`)**: A sequence of `10101010` to stabilize the microphone and synchronize the decoder's clock.
2.  **Start Frame (`0x02`)**: Indicates the beginning of the text message.
3.  **Message Body**: Each ASCII character is sent as a pure 8-bit block.
4.  **End Frame (`0x03`)**: Indicates the end of the transmission and triggers a message "flush" to the UI.

---

## 🏗️ Project Architecture

The AFSK logic is organized into three main modules in `commonMain`:

- **`AfskConfig.kt`**: Centralizes constants (Frequencies, Baud Rate, Frames).
- **`AfskProcessor.kt`**: Contains the `AfskEncoder` (PCM generation) and `AfskDecoder` (Zero-crossing analysis).
- **`AfskHelper.kt`**: Defines the interface for platform-specific implementations.

---

## 🧠 How the Decoder works?

The decoder uses **Zero-crossing with Hysteresis** to estimate frequency in real-time:
1. **Hysteresis (0.02f)**: Prevents noise from being counted as zero-crossings.
2. **Frequency Estimation**: Calculates the period between crossings to identify the current frequency.
3. **Bit Scoring**: Accrues a "frequency score" over the bit duration to decide if it's a 0 or 1.
4. **Byte Reconstruction**: Groups 8 bits into bytes and verifies start/end frames for message framing.

---

## 💻 How to Run

### Requirements
- Android Studio / IntelliJ IDEA
- Xcode (for iOS)
- Kotlin 2.1.0+

### Android
Run via terminal:
```bash
./gradlew :composeApp:assembleDebug
```

### iOS
Open the `iosApp` folder in Xcode or use the run configuration in Android Studio.

---

## 📄 License

This project is for educational and experimental purposes. Feel free to explore and contribute!

---
Developed with ❤️ using **Compose Multiplatform**.

![](https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExaHZhNGVyN3FtNjRlZWJqb2dyOW5renRtbG50d3o1YXZkZTVrNzJyYyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/10xp3IDPPOG1OM/giphy.gif)

&nbsp;
