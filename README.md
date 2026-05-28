# 🏁 Circuit Rush

Un juego de carreras estilo arcade desarrollado en Java + jMonkeyEngine 3, con sistema de drift, cámara dinámica, pista generada y efectos visuales como humo y marcas de llanta.

---

## 🎮 Gameplay

* 🚗 Control de carro arcade con movimiento suave e inercial  
* 💨 Sistema de drift con humo y skid marks  
* 🛣️ Circuito grande con curvas, rectas y paredes  
* 🌄 Skybox + iluminación dinámica  
* 📷 Cámara con múltiples modos (follow, cinematic, top)  
* 🖤 Marcas de llanta en el suelo (skid marks)

---

## 🛠️ Tecnologías

* Java 21  
* jMonkeyEngine 3.6.x  
* Gradle  
* LWJGL 3  
* Bullet Physics (en desarrollo / opcional)

---

## 🚀 Cómo ejecutar el proyecto

Clona el repositorio:

```bash
git clone https://github.com/tuusuario/circuit-rush.git
```
### Entrar al proyecto
```bash
cd Circuit-Rush
```
### Ejecutar en Windows
```bash
./gradlew run
```

---

## 🎮 Controles

* W → Acelerar  
* S → Frenar / reversa  
* A → Girar izquierda  
* D → Girar derecha  
* SPACE → Drift  
* TAB → Cambiar cámara  

---

## 📁 Estructura del proyecto

```
com.mygame
├── Main.java
├── entities
│   └── Car.java
├── builder
│   └── TrackBuilder.java
├── camera
│   └── CameraManager.java
├── input
│   └── InputHandler.java
├── world
│   └── LightManager.java
├── strategy
│   └── ArcadeDrive.java
├── factory
│   └── VehicleFactory.java
└── observer
    └── Subject / Observer
```

---

## 💡 Features

* Sistema de conducción arcade con inercia real  
* Drift con pérdida de control progresiva  
* Pista grande modular y expandible  
* Cámara suave con múltiples modos  
* Efectos de partículas (humo)  
* Skid marks dinámicos  
* Arquitectura limpia basada en patrones (Strategy + Observer + Factory)  

---

## ⚠️ Nota

Este proyecto está en desarrollo. Las físicas, controles y mecánicas pueden cambiar mientras se mejora la experiencia de conducción.

---

## 🧠 Autor

Tomás Montoya Buitrago - Michael Naranjo Chito

Proyecto de aprendizaje en Java + jMonkeyEngine
