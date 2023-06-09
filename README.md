# pwf-whisperer-microservice
Mikroslužba slouží pro získávání dat z DWH pro našeptávače v systému PowerFLOW. Vystupuje tak jako integrační mezivrstva.

## Struktura projektu
Projekt se skládá ze dvou submodulů:

* **app** - aplikační modul obsahující business logiku pro získání dat z DWH

## Build nového docker image
Tento odstavec popisuje kroky potřebné k vydání docker image s novou verzí aplikace.

1. Nastavit novou verzi v elementu `<version>` v `pom.xml`, `specification/pom.xml` a `app/pom.xml`.
2. Provést příkaz `mvn install` nad submodulem `specification`.
3. S aktivním maven profilem `docker` provést příkaz `mvn install`. Tento příkaz provede build nového docker image a 
   jeho následný upload do docker registry.
   
## OpenAPI UI
<http://localhost:8080/swagger-ui/index.html>