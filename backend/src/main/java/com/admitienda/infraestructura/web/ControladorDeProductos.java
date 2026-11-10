package com.admitienda.infraestructura.web;

import com.admitienda.dominio.modelo.Dinero;
import com.admitienda.dominio.puerto.entrada.GestionDeProductos;
import com.admitienda.infraestructura.web.dto.ProductoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Adaptador de entrada HTTP para el catálogo de productos.
 */
@RestController
@RequestMapping("/api/productos")
public final class ControladorDeProductos {

    private final GestionDeProductos productos;

    public ControladorDeProductos(GestionDeProductos productos) {
        this.productos = productos;
    }

    /** Cuerpo de la petición de creación o edición de producto. */
    record PeticionDeProducto(String tipoProducto, String nombreProducto,
                              double precioNeto, double ganancia,
                              int cantidadStock, int stockMinimo) {
    }

    @GetMapping
    public List<ProductoDto> listar(@RequestParam(required = false) String nombre) {
        return productos.listar(nombre).stream().map(ProductoDto::desde).toList();
    }

    @GetMapping("/{id}")
    public ProductoDto consultar(@PathVariable long id) {
        return ProductoDto.desde(productos.consultarPorId(id));
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public ProductoDto crear(@RequestBody PeticionDeProducto peticion,
                              HttpServletRequest pedido) {
        return ProductoDto.desde(productos.crear(
                Actores.de(pedido), datosDelProducto(peticion)));
    }

    @PutMapping("/{id}")
    public ProductoDto actualizar(@PathVariable long id,
                                  @RequestBody PeticionDeProducto peticion,
                                  HttpServletRequest pedido) {
        return ProductoDto.desde(productos.actualizar(
                Actores.de(pedido), id, datosDelProducto(peticion)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void eliminar(@PathVariable long id, HttpServletRequest pedido) {
        productos.eliminar(Actores.de(pedido), id);
    }

    private GestionDeProductos.ProductoPorCrear datosDelProducto(PeticionDeProducto peticion) {
        return new GestionDeProductos.ProductoPorCrear(
                peticion.tipoProducto(), peticion.nombreProducto(),
                Dinero.dePesos(peticion.precioNeto(), "precioNeto"),
                Dinero.dePesos(peticion.ganancia(), "ganancia"),
                peticion.cantidadStock(), peticion.stockMinimo());
    }
}
