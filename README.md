# sandbox-pi

My Raspberry Pi experiments


# Build

```bash
# Create UberJAR
sbt backend/assembly

# Create Docker image
sbt backend/docker:publishLocal

sbt frontend/fastLinkJS
sbt frontend/fullLinkJS
```

## Publish docker image

```bash
docker login
docker tag sandbox-pi:0.1.0-SNAPSHOT ghcr.io/minebreaker/sandbox-pi:latest
docker push ghcr.io/minebreaker/sandbox-pi:latest  
```
