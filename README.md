# sandbox-pi

My Raspberry Pi experiments


# Build

```bash
# Create UberJAR
sbt backend/assembly

cd frontend && npm run init
sbt frontend/fastLinkJS
sbt frontend/fullLinkJS
cd frontend && npm run build # this will copy the output js to the backend resource dir. TODO: should use generated dir

```

## Publish docker image

```bash
# FIXME: cross build is not working. must wait for sbt-native-packager to fix it.

# Create Docker image
sbt backend/docker:publishLocal

docker login
docker tag sandbox-pi:0.1.0-SNAPSHOT ghcr.io/minebreaker/sandbox-pi:latest
docker push ghcr.io/minebreaker/sandbox-pi:latest  
```
