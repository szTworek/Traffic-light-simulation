docker build --target build -t traffic-sim-test .
docker run --rm traffic-sim-test mvn test
