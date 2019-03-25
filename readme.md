**Spring Boot** ofrece un fantástico soporte para acceder a los datos con JPA a través de sus interfaces, del tipo [Repository](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/Repository.html). Si a esto le añadimos la facilidad con que se crean servicios REST, como explicaba en la entrada [http://www.profesor-p.com/2018/10/11/aplicacion-crud-en-kotlin-con-springboot/]( http://www.profesor-p.com/2018/10/11/aplicacion-crud-en-kotlin-con-springboot/) podremos hacer una aplicación ofreciendo una API para acceder a nuestra base de datos preferida con muy poco código.

Pero si queremos implementar [HATEOAS](https://spring.io/projects/spring-hateoas)  en nuestro proyecto p si hay muchos criterios sobre los que debemos acceder a los datos, deberemos escribir bastante código. Para solucionar este problema **Spring Boot** provee el paquete [Spring Data Rest](https://docs.spring.io/spring-data/rest/docs/current/reference/html/) con el cual con apenas código podremos crear una API para acceder a una tabla de nuestra base de datos.

En el ejemplo que planteare en esta entrada veremos como poder realizar el mantenimiento de una tabla de clientes (customers)  sin escribir ni una sola línea para definir las diferentes APIS REST.

### Creando el proyecto

Como siempre, podremos ir a la pagina  https://start.spring.io para crear nuestro proyecto **maven**. Para el proyecto de ejemplo que vamos a crear, deberemos incluir las siguientes dependencias:

**Spring Data Rest**, **H2**, **JPA**, **Lombok** como se ve en la siguiente captura de pantalla



![Creando el proyecto](.\starters.png)



Una vez tengamos hayamos importado el proyecto en nuestro IDE preferido, deberemos modificar el fichero **application.properties** para definir ciertos parámetros:

```
#H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2
#JPA
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=create-drop
# Datasource
spring.datasource.url=jdbc:h2:file:~/test
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
# Data Rest
spring.data.rest.base-path: /api
```

De este fichero la única entrada relativa a la librería **Data Rest** es la última. En ella especificamos la dirección donde se deben implementar las llamadas **REST** para acceder a las diferentes tablas que implemente nuestra aplicación. Es decir, en nuestro caso, se accederá a través de la URL: http://localhost:8080/api

Las demás líneas configuran la base de datos H2 que usaremos, así como ciertas propiedades de JPA. Por supuesto dejaremos a **JPA** que cree la estructura de la base de datos a traves de las entidades definidas, gracias a la sentencia: `spring.jpa.hibernate.ddl-auto=create-drop`



### Estructura del proyecto

Nuestro proyecto final tendrá la siguiente estructura:

![Estructura del proyecto](.\estructura.png)

Como se puede ver, definiremos dos tablas (entities) ,  que son: **City** y **Customer**. También definimos los correspondientes repositorios **CustomerRepository** y **CityRepository**

La clase **CityEntity.java** es la siguiente:

```java
@Entity
@Data
@RestResource(rel="customers", path="customer")
public class CustomerEntity {	
	@Id
	long id;	
	@Column
	String name;	
	@Column
	String address;	
	@Column
	String telephone;	
    @Column @JsonIgnore
	String secret;
	@OneToOne
	CityEntity city;    
}

```

Las particularidades de esta clase son las siguientes:

- la línea **@RestResource** donde con el parámetro **rel** especificamos como debe llamarse el objeto en la salida JSON. Con el parámetro **path** se indica donde se debe realizar la petición.
- La anotación **@JsonIgnore** aplicada a la columna *secret*.  Con esta etiqueta ese campo será ignorado, de tal manera que ni se mostrara en las salidas, ni se actualizara, aunque se incluya, en las peticiones.

Si no pusiéramos la etiqueta **@RestResource** Spring presentaría el recurso para acceder a la entidad en http://localhost:8080/api/customerEntities es decir usaría el nombre de la clase, poniéndolo en plural , por lo cual le añade '*es'*.

El repositorio de esta entidad esta en **CustomerRepository** y contiene solo estas líneas:

```java
public interface CustomerRepository  extends CrudRepository<CustomerEntity, Long>  {	
	public List<CustomerEntity> findByNameIgnoreCaseContaining(@Param("name") String name);
}
```

La función **findByNameIgnoreCaseContaining** que he definido permitirá buscar los clientes, ignorando mayúsculas y minúsculas, cuyo nombre contengan la cadena mandada. Más adelante explicare como poder realizar una consulta a través de esta llamada con **Spring Data Rest**

Tenéis más documentación sobre como crear consultas personalizadas en Spring en [esta otra entrada mía](http://www.profesor-p.com/2018/08/25/jpa-hibernate-en-spring/).

La clase CityEntity.java contiene las siguientes líneas:

```java
@Entity
@Data
@RestResource(exported=false)
public class CityEntity {		
	@Id 
	int id;
	@Column
	String name;
	@Column
	String province;
}
```

En este caso la etiqueta **@RestResource** tiene indicada la propiedad **exported** igual a *false* . De esta manera esta entidad no será tratada por la **Data Rest** y no se implementara ninguna API para ella.

### Accediendo al API de Data Rest

Los recursos que están publicados estarán disponibles en la URL: http://localhost:8080/api, como hemos definido en el fichero **application.properties**. Esta será la salida en este proyecto:

```
> curl -s http://localhost:8080/api/
{
  "_links" : {
    "customers" : {
      "href" : "http://localhost:8080/api/customer"
    },
    "profile" : {
      "href" : "http://localhost:8080/api/profile"
    }
  }
}
```

El  *profile* se refiere a lo definido en el [RFC 6906](https://tools.ietf.org/html/rfc6906), donde se incluye detalles de la aplicación, pero no tratare de ello en esta entrada.

Así para acceder al único recurso disponible en nuestro proyecto, al que hemos llamado **customer**, navegaremos a: http://localhost:8080/api/customer .

Primero añadamos un registro. Esto lo realizaremos realizando una petición tipo **POST** como la siguiente:

```
> curl -s --request POST localhost:8080/api/customer -d '{"id": 1, "name":"nombre cliente 1","address":"direccion cliente 1","telephone":"telefono cliente 1", "secret": "no guardar"}' -H "Content-Type: application/json"
{
  "name" : "nombre cliente 1",
  "address" : "direccion cliente 1",
  "telephone" : "telefono cliente 1",
  "city" : null,
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/api/customer/1"
    },
    "customerEntity" : {
      "href" : "http://localhost:8080/api/customer/1"
    }
  }

```



Podemos comprobar que ha realizado la inserción gracias a la consola de H2. Para ello iremos a la URL http://localhost:8080/h2/ y pulsaremos el botón **Connect**. Una vez conectados, si realizamos una *query* sobre la tabla CUSTOMER_ENTITY veremos la siguiente salida:

![Consultando datos en tabla customer_entity](.\consola-h2-query.png)

Observar que aunque hemos añadido el valor para el campo "**secret**"  este  no se ha guardado en la base de datos.

Como no podemos acceder a la tabla  **city_entity** a través de nuestra API pues así lo hemos especificado, Vamos a aprovechar que estamos en la consola y añadir un registro en la tabla, el cual asignaremos al cliente insertado.

```sql
insert into city_entity values(1,'Logroño','La Rioja');
update customer_entity set city_id=1;
```



![Consultando datos en tabla customer_entity](.\consola-h2.png)



Ahora si realizamos una petición **GET**  obtendríamos la siguiente salida:

```
 curl -s localhost:8080/api/customer
{
  "_embedded" : {
    "customers" : [ {
      "name" : "nombre cliente 1",
      "address" : "direccion cliente 1",
      "telephone" : "telefono cliente 1",
      "city" : {
        "name" : "Logroño",
        "province" : "La Rioja"
      },
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/api/customer/1"
        },
        "customerEntity" : {
          "href" : "http://localhost:8080/api/customer/1"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/api/customer"
    },
    "profile" : {
      "href" : "http://localhost:8080/api/profile/customer"
    },
    "search" : {
      "href" : "http://localhost:8080/api/customer/search"
    }
  }
}
```

Observar como HATEOAS muestra los diferentes enlaces disponibles. 

Por ejemplo podríamos consultar directamente el cliente con el código 1 navegando a http://localhost:8080/api/customer/1

```
> curl -s localhost:8080/api/customer/1
{
  "name" : "nombre cliente 1",
  "address" : "direccion cliente 1",
  "telephone" : "telefono cliente 1",
  "city" : {
    "name" : "Logroño",
    "province" : "La Rioja"
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/api/customer/1"
    },
    "customerEntity" : {
      "href" : "http://localhost:8080/api/customer/1"
    }
  }
}

```

Como he explicado antes, en el repositorio de **Customer** he definido la función **findByNameIgnoreCaseContaining** . Para realizar una consulta usando esa función navegaremos a 

http://localhost:8080/api/customer/search/findByNameIgnoreCaseContaining{?name}

```
> curl -s http://localhost:8080/api/customer/search/findByNameIgnoreCaseContaining?name=Clien
{
  "_embedded" : {
    "customers" : [ {
      "name" : "nombre cliente 1",
      "address" : "direccion cliente 1",
      "telephone" : "telefono cliente 1",
      "city" : {
        "name" : "Logroño",
        "province" : "La Rioja"
      },
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/api/customer/1"
        },
        "customerEntity" : {
          "href" : "http://localhost:8080/api/customer/1"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/api/customer/search/findByNameIgnoreCaseContaining?name=Clien"
    }
  }
}
```

Por no hacer más larga la entrada no explicare como actualizar un registro, con peticiones HTTP tipo **PUT**, borrar registros con peticiones HTTP tipo **DELETE** o actualizar parte de un registro con **PATCH**, pues creo que es obvio y lo dejo como ejercicio para el lector.

Comentar  que lo mostrado en este articulo es solo un esbozo de la potencia de **Spring Data Rest**. 

Estas son algunas de las otras muchas características que implementa:

- Permite añadir eventos de tal manera que cuando se inserte, modifique, borre o incluso consulte un registro ese evento sea disparado y se puedan ejecutar el código deseado, el cual incluso puede modificar o anular la petición realizada. Por supuesto soporta navegación
- Soporta navegación entre los registros consultados.
- Validación de los datos insertados
- Los Links pueden ser totalmente personalizados.

Y esto es todo, esperando que el articulo haya resultado interesante me despido hasta el siguiente. Como siempre se agradecerá cualquier *feedback*.



