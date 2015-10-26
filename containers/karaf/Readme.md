# Apache Karaf

- homepage: http://karaf.apache.org/

supported versions:
- 3.0.x
- 4.0.x

## Usage
```java
final KarafConfiguration conf = KarafConfiguration.builder().directory($KARAF_HOME).xmx("2g").build();

try (Container cont = new KarafContainer<>(conf)) {
	cont.start()
	try(Client cli = container.getClient()) {
		cli.execute("info");
	}
}
```
Standalone client:
```java
try (Client client = new KarafClient<>(KarafConfiguration.builder().build())) {
	client.execute("version");
}
```
