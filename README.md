# Proyecto Farmatodo Implementando con Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**

## Requisitos Previos

- Java 21
- Gradle 8.4
- Docker y docker compose
- Acceso al repositorio 
- Acceso al proyecto en GCP

## Ejecución Local

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/SebastianGironArcila/farmatodo-app
   cd tu-repo

2. Compila el proyecto
   ```bash
   ./gradlew clean build

3. Ejecuta la aplicación:
   ```bash
   ./gradlew bootRun

4. Accede a la documentacion Swager
   ```bash
   http://localhost:8080/webjars/swagger-ui/index.html


# Docker local
- Build y run 
  ```bash
  docker compose up --build farmatodo/deployment

## Despliegue automático con GitHub Actions a Cloud Run

No necesitas autenticación manual en GCP para desplegar. El pipeline de GitHub Actions ya está configurado para construir, testear, publicar la imagen en Artifact Registry y desplegar en Cloud Run automáticamente según la rama.

- Rama develop -> despliega a ambiente dev:
URL: https://java-api-service-dev-578301564702.us-central1.run.app
- Rama master -> despliega a ambiente prod:
URL: https://java-api-service-578301564702.us-central1.run.app

# Flujo:

1. Push a develop o master dispara el workflow CI/CD.
2. Jobs:
- Build: tests, cobertura, empaquetado y push de imagen Docker.
- Deploy dev: si la rama es develop.
- Deploy prod: si la rama es master.
3. Variables y entorno:
Usa deployment/env.dev.yaml o deployment/env.prod.yaml para configurar SECURITY_API_KEY, perfiles, etc.
4. Cómo promover y revertir:

- Promover a prod: merge de develop a master.
- Rollback rápido: desde Cloud Run selecciona una revisión anterior o haz revert del commit y push a la rama correspondiente.
5. Ver estado de despliegue:
- Revisa la pestaña Actions en GitHub.
  ```bash
  https://github.com/SebastianGironArcila/farmatodo-app/actions
- Logs de la app en Cloud Run (Log Explorer de GCP).

# Colecciones de Postman

## Ubicación de archivos en el repositorio

**Colección:**
- `docs/postman/Farmatodo API.postman_collection.json`

**Ambientes:**
- `docs/postman/env/Farmatodo Local.postman_environment.json`
- `docs/postman/env/Farmatodo Dev.postman_environment.json`
- `docs/postman/env/Farmatodo Prod.postman_environment.json`

## Cómo importar en Postman

1. **Importar la colección**  
   Postman > Import > Files > selecciona  
   `docs/postman/Farmatodo API.postman_collection.json`

2. **Importar uno o más ambientes**  
   Postman > Import > Files > selecciona uno o varios de:
    - `docs/postman/env/Farmatodo Local.postman_environment.json`
    - `docs/postman/env/Farmatodo Dev.postman_environment.json`
    - `docs/postman/env/Farmatodo Prod.postman_environment.json`

3. **Seleccionar el ambiente**  
   En la esquina superior derecha de Postman, abre el selector de ambientes y elige:
    - Farmatodo Local
    - Farmatodo Dev
    - Farmatodo Prod

## Variables por ambiente

- **baseUrl**
    - Local: `http://localhost:8080`
    - Dev: `https://java-api-service-dev-578301564702.us-central1.run.app`
    - Prod: `https://java-api-service-578301564702.us-central1.run.app`
- **apiKey:** `farmatodo-secret-key-2024`
- **clientId:** `1`
- **productName:** vacío por defecto

## Autenticación

- La colección ya está configurada con **API Key** en el header `X-API-KEY` usando la variable `{{apiKey}}` del ambiente.
- Verifica que cada request herede la auth de la colección:
    - **Collection > Authorization > Type:** API Key
    - En cada request: pestaña **Settings > “Inherit auth from parent”**

## Prueba rápida

1. Selecciona el ambiente deseado.
2. Ejecuta **GET Ping**:  
   `{{baseUrl}}/api/v1/ping`
3. Para rutas protegidas, Postman enviará `X-API-KEY` automáticamente desde la colección.

## Notas

- Si cambias el valor real del **API Key** en el backend, actualiza la variable `apiKey` en el ambiente correspondiente.
- Puedes editar `clientId` y `productName` en **Variables del ambiente** para tus pruebas.


## Notas finales

- Para que Swagger permita setear el X-API-KEY, el proyecto define un Security Scheme ApiKeyAuth. Usa el botón Authorize. La que viene por defecto en la configuracion es: farmatodo-secret-key-2024
- El filtro ApiKeyAuthFilter aplica solo a /api/v1/**, por lo que Swagger y Actuator no requieren header.
- En ambientes local,dev y prod se usa H2 en memoria.


