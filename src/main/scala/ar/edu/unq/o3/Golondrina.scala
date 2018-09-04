package ar.edu.unq.o3


class Golondrina {
  var energia = 100.0

  def volar(kms: Int) = {
    this.energia = this.energia - kms * 0.5
  }

  def comer(gramos: Int) = {
    this.energia = this.energia + gramos * 1.2
  }

}