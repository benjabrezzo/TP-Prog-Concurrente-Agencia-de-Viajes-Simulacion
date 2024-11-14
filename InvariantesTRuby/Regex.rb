def check_transition_invariants
  # Usa barras invertidas dobles para escapar correctamente en rutas de Windows
  file_path = '/home/benja/Programacion/Java/TPF_PC/transiciones.log'

  # Abre el archivo y lee el contenido
  tt = File.open(file_path, 'r') { |f| f.read.chomp }

  # Define la expresión regular
  r = /(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(T10)|(T7)(.*?)(T8))(.*?)(T11)/

  # Inicializar un hash para contar las ocurrencias de cada grupo
  group_counts = {
    "GRUPO1" => 0,
    "GRUPO2" => 0,
    "GRUPO3" => 0,
    "GRUPO4" => 0
  }

  # Aplica la sustitución mientras sea posible
  while tt.match?(r)
    # Encuentra y reemplaza el primer match
    tt.sub!(r) do |match|
      # Determinar qué grupo es
      if $6 && $14
        group_id = "GRUPO4" # T0T1T2T5T6T9T10T11
      elsif $6
        group_id = "GRUPO3" # T0T1T2T5T7T8T11
      elsif $14
        group_id = "GRUPO2" # T0T1T3T4T6T9T10T11
      else
        group_id = "GRUPO1" # T0T1T3T4T7T8T11
      end
      group_counts[group_id] += 1

      # Reconstruir la cadena sin el grupo actual
      "#{$2.strip} #{$4.strip} #{$6 ? $7.strip : $10.strip} #{$12.strip} #{$14 ? "#{$15.strip} #{$17.strip}" : $20.strip} #{$22.strip}".gsub(/\s+/, ' ').strip
    end
  end

  # Limpieza final más estricta
  # tt = tt.split(' ').select { |part| part.match?(/^T[1-9]$|^T10$|^T11$/) }.join(' ')

  # Imprime la cadena resultante
  puts "Remaining string: '#{tt}'"
  # Imprime los conteos de los grupos
  group_counts.each do |group, count|
    puts "#{group}: #{count} occurrence(s)"
  end
end

# Llamar a la función
check_transition_invariants

