#! /usr/bin/guile \
-e main -s
!#

(use-modules 
	     (ice-9 getopt-long)
	     (ice-9 hash-table)
	     (ice-9 match)
	     (ice-9 q)
	     (ice-9 rdelim)
	     (ice-9 regex)
	     (json)
	     (srfi srfi-1)
	     (srfi srfi-9)
	     (srfi srfi-42)
	     (srfi srfi-43))

(define (vertex? line) (string-match "^v " line))
(define (normal? line) (string-match "^vn " line))
(define (face? line) (string-match "^f " line))

(define double-regexp (make-regexp "-*[0-9\\.]+"))
(define (collect-double line)
  (map (lambda (ms) (string->number (match:substring ms)))
       (list-matches double-regexp line)))

(define int-regexp (make-regexp "[0-9]+"))
(define (collect-int line)
  (map (lambda (ms) (string->number (match:substring ms)))
       (list-matches int-regexp line)))

(define (collect-index substr) ;; "1/2/3" "4//1" "2"
  (match (collect-int substr)
	 (#nil #f)
	 ((i) (cons i i))
	 ((i j) (if (string-contains substr "//")
		    (cons i j)
		    (cons i i)))
	 ((i j k) (cons i k))))

(define face-regexp (make-regexp "[0-9/]+"))
(define (collect-face line)                           ;; "f 1/2/3 4//1 2"
  (list-ec (: ms (list-matches face-regexp line))     ;; ("1/2/3" "4//1" "2") 
	   (:let substr (match:substring ms))
	   (collect-index substr)))                   ;; ((1 . 3) (4 . 1) (2 . 2))

(define-record-type <reader>
  (make-reader filename vertices normals faces)
  reader?
  (filename reader-filename)
  (vertices reader-vertices)
  (normals  reader-normals)
  (faces    reader-faces))

(define (create-reader filename)
  (make-reader filename (make-q) (make-q) (make-q)))

(define (reader-read-line reader line)
  (match line
	 ((? vertex? l) (q-push! (reader-vertices reader) (collect-double l))) ;; (x y z)
	 ((? normal? l) (q-push! (reader-normals reader)  (collect-double l))) ;; (nx ny nz)
	 ((? face? l)   (q-push! (reader-faces reader)    (collect-face l)))   ;; ((1 . 3)(2 . 2)(4 . 1))
	 (l #f)))

(define (reader-read reader)
  (call-with-input-file (reader-filename reader)
    (lambda (port)
      (do-ec (:port line port read-line)
	     (reader-read-line reader line)))))

(define (reader-dump reader)
  (let* ((vertices (vector-reverse-copy (list->vector (car (reader-vertices reader)))))
	 (normals  (vector-reverse-copy (list->vector (car (reader-normals  reader)))))
	 (faces                                       (car (reader-faces    reader)))
	 (polygons (fold-ec #nil (: face faces)
			    (:let data (list-ec (: indices face)
						(:let vertex (vector-ref vertices (- (car indices) 1)))
						(:let normal (vector-ref normals (- (cdr indices) 1)))
						(alist->hash-table (list (cons "pos" vertex)
									 (cons "normal" normal)))))
			    (alist->hash-table (list (cons "polygon" data)))
			    cons))
	 (name (basename (reader-filename reader) ".obj")))
    (cons name polygons)))

(define (parse-file filename)
  (let ((reader (create-reader filename)))
    (reader-read reader)
    (reader-dump reader)))

(define (parse-files filelist)
  (let ((records (list-ec (: filename filelist)
			  (parse-file filename))))
    (alist->hash-table records)))

(define option-spec
  '((help (single-char #\h) (value #f))))

(define (main args)
  (let* ((options (getopt-long args option-spec))
	 (help (option-ref options 'help #f))
	 (filelist (option-ref options '() #nil)))
    (display "var db = `\n")
    (scm->json (parse-files filelist) #:pretty #t)
    (display "`\n")
    ))
