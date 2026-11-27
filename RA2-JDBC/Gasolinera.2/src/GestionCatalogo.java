import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Catálogo y acuerdos de suministro.
 *
 * Durante un alta en varios pasos puede haber elementos todavía sin acuerdo;
 * las operaciones públicas que confirman el alta comprueban que nadie quede
 * aislado. En MySQL hará falta una transacción con esta misma validación final.
 */
public final class GestionCatalogo {

    private final Set<Producto> productos = new LinkedHashSet<>();
    private final Set<Proveedor> proveedores = new LinkedHashSet<>();
    private final Set<AcuerdoSuministro> acuerdos = new LinkedHashSet<>();

    public GestionCatalogo(Collection<Producto> productosIniciales,
                           Collection<Proveedor> proveedoresIniciales,
                           Collection<AcuerdoSuministro> acuerdosIniciales) {
        Objects.requireNonNull(productosIniciales).forEach(this::registrarProducto);
        Objects.requireNonNull(proveedoresIniciales).forEach(this::registrarProveedor);
        Objects.requireNonNull(acuerdosIniciales).forEach(this::registrarAcuerdo);
        validarSinAislados();
    }

    public Producto registrarProducto(Producto producto) {
        Objects.requireNonNull(producto, "El producto es obligatorio.");
        if (productos.contains(producto)) {
            throw new IllegalArgumentException("Ya existe el producto " + producto.getReferenciaComercial() + ".");
        }
        productos.add(producto);
        return producto;
    }

    public Proveedor registrarProveedor(Proveedor proveedor) {
        Objects.requireNonNull(proveedor, "El proveedor es obligatorio.");
        if (proveedores.contains(proveedor)) {
            throw new IllegalArgumentException("Ya existe el proveedor " + proveedor.getCodigoInterno() + ".");
        }
        proveedores.add(proveedor);
        return proveedor;
    }

    public AcuerdoSuministro registrarAcuerdo(AcuerdoSuministro acuerdo) {
        Objects.requireNonNull(acuerdo, "El acuerdo es obligatorio.");
        if (!proveedores.contains(acuerdo.getProveedor()) || !productos.contains(acuerdo.getProducto())) {
            throw new IllegalArgumentException("Proveedor y producto deben estar registrados.");
        }
        if (acuerdos.contains(acuerdo)) {
            throw new IllegalArgumentException("Ese acuerdo ya está vigente.");
        }
        acuerdos.add(acuerdo);
        return acuerdo;
    }

    /** Alta conjunta: proveedor nuevo con al menos un acuerdo. */
    public Proveedor darDeAltaProveedor(Proveedor proveedor, AcuerdoSuministro primerAcuerdo) {
        registrarProveedor(proveedor);
        try {
            registrarAcuerdo(primerAcuerdo);
            validarSinAislados();
            return proveedor;
        } catch (RuntimeException e) {
            acuerdos.removeIf(a -> a.getProveedor().equals(proveedor));
            proveedores.remove(proveedor);
            throw e;
        }
    }

    /** Alta conjunta: producto nuevo con al menos un acuerdo. */
    public Producto darDeAltaProducto(Producto producto, AcuerdoSuministro primerAcuerdo) {
        registrarProducto(producto);
        try {
            registrarAcuerdo(primerAcuerdo);
            validarSinAislados();
            return producto;
        } catch (RuntimeException e) {
            acuerdos.removeIf(a -> a.getProducto().equals(producto));
            productos.remove(producto);
            throw e;
        }
    }

    public void eliminarAcuerdo(AcuerdoSuministro acuerdo) {
        if (!acuerdos.remove(acuerdo)) {
            throw new IllegalArgumentException("Ese acuerdo no está registrado.");
        }
        try {
            validarSinAislados();
        } catch (RuntimeException e) {
            acuerdos.add(acuerdo);
            throw e;
        }
    }

    public List<AcuerdoSuministro> acuerdosDe(Proveedor proveedor) {
        return acuerdos.stream().filter(a -> a.getProveedor().equals(proveedor)).collect(Collectors.toList());
    }

    public List<AcuerdoSuministro> acuerdosDe(Producto producto) {
        return acuerdos.stream().filter(a -> a.getProducto().equals(producto)).collect(Collectors.toList());
    }

    public Set<Producto> getProductos() { return Collections.unmodifiableSet(productos); }
    public Set<Proveedor> getProveedores() { return Collections.unmodifiableSet(proveedores); }
    public Set<AcuerdoSuministro> getAcuerdos() { return Collections.unmodifiableSet(acuerdos); }

    private void validarSinAislados() {
        boolean proveedorAislado = proveedores.stream()
                .anyMatch(p -> acuerdos.stream().noneMatch(a -> a.getProveedor().equals(p)));
        boolean productoAislado = productos.stream()
                .anyMatch(p -> acuerdos.stream().noneMatch(a -> a.getProducto().equals(p)));
        if (proveedorAislado || productoAislado) {
            throw new IllegalStateException("Ningún proveedor ni producto puede quedar sin acuerdo.");
        }
    }
}
