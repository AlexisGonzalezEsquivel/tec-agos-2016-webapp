package alumnos

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class AlumnoController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Alumno.list(params), model:[alumnoCount: Alumno.count()]
    }

    def show(Alumno alumno) {
        respond alumno
    }

    def create() {
        respond new Alumno(params)
    }

    @Transactional
    def save(Alumno alumno) {
        if (alumno == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (alumno.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond alumno.errors, view:'create'
            return
        }

        alumno.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'alumno.label', default: 'Alumno'), alumno.id])
                redirect alumno
            }
            '*' { respond alumno, [status: CREATED] }
        }
    }

    def edit(Alumno alumno) {
        respond alumno
    }

    @Transactional
    def update(Alumno alumno) {
        def alumnoGuardado = Alumno.get(alumno.id)
        if (alumnoGuardado == null) {
            save(alumno)  
        }else{
            def versionMaxima = Alumno.withCriteria{
                projections{
                    max 'version'
                }
            }
            versionMaxima+=1
            new Alumno(matricula:alumnoGuardado.matricula,
                      salon:alumnoGuardado.salon,
                      version: versionMaxima).save()

        }

        if (alumno.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond alumno.errors, view:'edit'
            return
        }

        alumno.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'alumno.label', default: 'Alumno'), alumno.id])
                redirect alumno
            }
            '*'{ respond alumno, [status: OK] }
        }
    }

    @Transactional
    def delete(Alumno alumno) {

        if (alumno == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        alumno.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'alumno.label', default: 'Alumno'), alumno.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'alumno.label', default: 'Alumno'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
