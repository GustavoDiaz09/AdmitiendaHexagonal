package com.admitienda.infraestructura.web;

import com.admitienda.dominio.modelo.excepciones.ConflictoDeDatosException;
import com.admitienda.dominio.modelo.excepciones.CredencialesInvalidasException;
import com.admitienda.dominio.modelo.excepciones.ExcepcionDeDominio;
import com.admitienda.dominio.modelo.excepciones.PermisoDenegadoException;
import com.admitienda.dominio.modelo.excepciones.RecursoNoEncontradoException;
import com.admitienda.infraestructura.web.dto.ErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traductor de excepciones a respuestas HTTP del contrato: cualquier
 * fallo responde {@code 4xx/5xx} con un único campo "error" legible.
 * Las excepciones del dominio viajan hasta aquí sin que los casos de
 * uso sepan nada de HTTP.
 */
@RestControllerAdvice
public final class ErroresGlobales {

    private static final Logger bitacora = LoggerFactory.getLogger(ErroresGlobales.class);

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorDto credencialesInvalidas(CredencialesInvalidasException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(PermisoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorDto permisoDenegado(PermisoDenegadoException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto recursoNoEncontrado(RecursoNoEncontradoException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(ConflictoDeDatosException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDto conflicto(ConflictoDeDatosException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(ExcepcionDeDominio.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto reglaDeNegocio(ExcepcionDeDominio e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto peticionInvalida(Exception e) {
        return new ErrorDto("La petición no es válida.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto inesperado(Exception e) {
        bitacora.error("Fallo no controlado en la API.", e);
        return new ErrorDto("Ocurrió un error inesperado. Intenta de nuevo.");
    }
}
