import java.time.LocalDate;
import java.util.Objects;

/** Condición específica de un expendedor: acreditación para manipular combustibles. */
public record Expendedor(String numeroAcreditacion, LocalDate caducidad) implements Funcion {

    public Expendedor {
        if (numeroAcreditacion == null || numeroAcreditacion.isBlank()) {
            throw new IllegalArgumentException("El número de acreditación es obligatorio.");
        }
        numeroAcreditacion = numeroAcreditacion.trim();
        Objects.requireNonNull(caducidad, "La caducidad de la acreditación es obligatoria.");
    }

    @Override public String codigo() { return "EXPENDEDOR"; }

    @Override public String descripcion() {
        return "Expendedor (acreditación " + numeroAcreditacion + ", caduca " + caducidad + ")";
    }
}
