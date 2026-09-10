# Arena Elemental

Projeto pessoal: um jogo de captura e batalha de criaturas por turnos, com
interface gráfica em Java Swing, inspirado no gênero de Pokémon e construído
sem nenhum elemento autoral da franquia.

## Sobre as criaturas

As criaturas deste projeto (**Braseiro**, **Marulho**, **Folharal**, **Calhau**,
**Nevisco**, **Penumbra**, **Candeio**, **Breumar**, **Tocumbra**, **Brasalto**),
os golpes e a arte de
todos os sprites são **originais**, desenhadas em código com Java2D — nenhuma
imagem, nome ou personagem da franquia Pokémon foi usado, já que é propriedade
da Nintendo/Game Freak. A mecânica (tipos elementares, captura, equipe, batalha
por turnos) é inspirada no gênero, não na obra específica.

> **As imagens de fundo das áreas são a exceção.** Elas não fazem parte deste
> repositório e não são arte original — ver [Cenários](#cenários).
> Tudo o que o jogo desenha por conta própria continua sendo código.

## Estrutura do projeto

```
src/arenaelemental/
├── Main.java                  → ponto de entrada
├── modelo/
│   ├── TipoElemental.java     → os sete tipos e a tabela de efetividade
│   ├── CategoriaGolpe.java    → Físico / Especial / Status
│   ├── CondicaoStatus.java    → queimadura, paralisia, veneno, sono, congelamento
│   ├── Golpe.java             → id, tipo, categoria, poder, precisão, PP e efeito
│   ├── Golpes.java            → catálogo de golpes, indexado por id
│   ├── AprendizadoGolpe.java  → em que nível cada golpe é aprendido
│   ├── EstatisticasBase.java  → os seis status base da espécie
│   ├── GrupoExperiencia.java  → curvas de experiência (n³ e companhia)
│   ├── Experiencia.java       → quanto rende derrotar cada criatura
│   ├── Aparencia.java         → cor e adorno com que a espécie é desenhada
│   ├── EspecieCriatura.java   → a ficha de uma espécie, montada por builder
│   ├── Especies.java          → o registro de todas as espécies e o sorteio por área
│   ├── Criatura.java          → classe abstrata base (status, nível, EXP, golpes, PP, condição)
│   ├── CriaturaComum.java     → criatura sem habilidade especial
│   ├── Braseiro.java          → tipo Fogo (habilidade: Labareda)
│   ├── Marulho.java           → tipo Água (habilidade: Maré Cheia)
│   ├── Folharal.java          → tipo Planta (habilidade: Sugar Seiva)
│   ├── Calhau.java            → tipo Pedra (habilidade: Rolo Compressor)
│   ├── Nevisco.java           → tipo Gelo (habilidade: Frio Cortante)
│   ├── Penumbra.java          → tipo Sombrio (habilidade: Emboscada)
│   ├── Candeio.java           → tipo Sagrado (habilidade: Bênção)
│   ├── Breumar.java           → Sombrio / Água, Mar Profundo (habilidade: Isca Luminosa)
│   ├── Tocumbra.java          → Sombrio / Planta, Floresta Profunda (habilidade: Emaranhado)
│   ├── Brasalto.java          → Fogo / Pedra, Vulcão (habilidade: Erupção)
│   └── FabricaCriaturas.java  → cria iniciais e os encontros selvagens de cada área
├── mundo/
│   ├── Area.java              → as áreas do mapa e as rotas entre elas
│   └── Habitats.java          → onde cada tipo de criatura vive (comum ou raro)
├── treinador/
│   ├── Treinador.java         → equipe (ordem, líder, troca) e a área onde o jogador está
│   └── Bestiario.java         → espécies vistas e capturadas
├── batalha/
│   ├── CalculadoraDano.java   → a fórmula de dano da 9ª geração
│   ├── ResultadoDano.java     → dano + crítico + efetividade + STAB + rolagem
│   ├── Batalha.java           → turnos, ordem, precisão, condições, troca, EXP e captura
│   └── Captura.java           → cálculo da chance de captura
├── persistencia/
│   ├── RepositorioJogo.java   → grava e lê a partida em disco (um arquivo)
│   ├── Saves.java             → os slots de save: qual arquivo é de qual slot, resumo e o mais recente
│   ├── SlotDeSave.java        → um slot numerado ou o do salvamento rápido
│   ├── ResumoDoSave.java      → o que a lista de slots mostra de cada um
│   ├── JogoSalvo.java         → a partida lida do arquivo, mais os avisos
│   └── ErroDePersistencia.java → falha de leitura ou gravação, com mensagem
└── view/                      → toda a interface gráfica (Swing)
    ├── Constantes.java        → paleta do tema escuro e as cores vívidas dos tipos
    ├── BotaoJogo.java         → o botão com gradiente vívido, pintado à mão
    ├── BarraRolagemEscura.java → barra de rolagem fina, no tom do tema
    ├── CriaturaSprite.java    → desenho original de cada espécie
    ├── BarraVida.java         → cartão sobre a arena: nome, nível, condição, vida e EXP
    ├── Cenario.java           → uma imagem de fundo e a área ou o tipo a que ela pertence
    ├── Cenarios.java          → descobre a pasta de imagens e sorteia o cenário da área
    ├── PainelArena.java       → a cena: cenário da área, criaturas e cartões de vida
    ├── CartaoCriatura.java    → cartão usado na tela de escolha inicial
    ├── PainelMenu.java        → tela inicial (nova jornada, continuar, carregar, sair)
    ├── PainelSaves.java       → lista de slots, para salvar ou carregar
    ├── PainelEscolha.java     → escolha da criatura inicial
    ├── PainelBatalha.java     → tela principal (exploração, rotas, golpes, troca)
    ├── PainelEquipe.java      → equipe editável (ordem e líder) + bestiário
    └── JanelaPrincipal.java   → janela principal (CardLayout entre as telas)

test/arenaelemental/           → testes unitários (JUnit 5), mesma estrutura de pacotes
docs/                          → renders das telas (menu, exploração, rotas, batalha, equipe)
imagens de referencia/         → cenários, uma subpasta por área (fora do repositório — ver abaixo)
```

## Como compilar e rodar

Requer JDK 11 ou superior (testado com JDK 21).

```bash
# a partir da raiz do projeto
javac -encoding UTF-8 -d build $(find src -name "*.java")
java -cp build arenaelemental.Main
```

No PowerShell, o equivalente ao `find`:

```powershell
javac -encoding UTF-8 -d build (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp build arenaelemental.Main
```

> O `-encoding UTF-8` é necessário: os fontes têm acentos e o `javac` no Windows
> assume a codificação da região, não UTF-8.

## Como rodar os testes unitários

Os testes usam JUnit 5 (Jupiter) e não há jar no repositório. O jeito mais
simples sem Maven/Gradle é baixar o **JUnit Platform Console Standalone** e rodar:

```bash
javac -encoding UTF-8 -d build $(find src -name "*.java")
javac -encoding UTF-8 -cp "build;junit-platform-console-standalone.jar" -d build-test $(find test -name "*.java")
java -jar junit-platform-console-standalone.jar execute -cp "build;build-test" --scan-classpath
```

O separador de classpath é `;` no Windows e `:` no Linux/macOS.

Se preferir Maven/Gradle, aponte `src` como source root, `test` como test root e
adicione a dependência `org.junit.jupiter:junit-jupiter:5.10.0` (ou mais recente)
no escopo de teste.

## A tela principal

Tema escuro em todas as telas. A tela principal tem três faixas: a **arena** em
cima, com os cartões de vida flutuando sobre o cenário; o **log** no meio; e o
**painel de ações** embaixo, que troca de conteúdo conforme o momento:

| Momento | Painel de ações |
|---|---|
| **Exploração** | **Explorar** em destaque, na cor da área, e ao lado **Viajar**, **Descansar**, **Equipe** e **Salvar / Sair** |
| **Rotas** | um botão por destino, na cor de cada área, e **Voltar** |
| **Batalha** | os golpes numa grade 2×2, na cor vívida do tipo, com **Capturar**, **Trocar** e **Fugir** pequenos, um acima do outro, ao lado |
| **Troca** | as seis casas da equipe; **Cancelar** some quando a troca é obrigatória |

O botão de golpe mostra o tipo numa etiqueta, a categoria, o poder, a precisão
e os PP. Sem PP ele fica cinza e desabilitado.

Os botões são pintados à mão (`BotaoJogo`) em vez de usar o fundo do `JButton`,
que muda de um *look and feel* para outro. Sobre cores claras, como a da Praia
ou a do Palácio Sagrado, o texto fica escuro sozinho: a escolha sai da
luminância da cor.

### Equipe

O botão **Equipe** abre a equipe editável e o bestiário. Em cada cartão, as
setas **←** e **→** mudam a criatura de posição e **Tornar líder** a leva para a
frente. A líder é quem entra em campo ao explorar, e continua sendo depois de
descansar. A ordem vai para o jogo salvo.

## Mapa e rotas

A jornada começa na **Floresta**. **Explorar** procura uma criatura selvagem na
área atual; **Viajar** mostra as rotas que saem dela.

| De | Pode ir para |
|---|---|
| Floresta | Floresta Profunda, Montanha, Ravina |
| Floresta Profunda | Floresta |
| Montanha | Floresta, Montanha Negra, Vulcão |
| Vulcão | Montanha |
| Montanha Negra | Montanha, Palácio Sagrado |
| Palácio Sagrado | qualquer outra área |
| Ravina | Floresta, Ravina Congelada, Mar |
| Ravina Congelada | Ravina, Mar |
| Praia | Mar, Ravina |
| Mar | Mar Profundo, Ravina Congelada, Praia |
| Mar Profundo | Mar |

**As rotas são de mão única.** A Ravina leva ao Mar, mas do Mar não se volta
direto para a Ravina: o caminho de volta passa pela Ravina Congelada. O botão de
cada destino avisa quando não há rota direta de volta. Mesmo assim, de toda área existe algum caminho até a Floresta, e o
`AreaTest` garante isso: como o jogo salvo guarda a área, uma região sem saída
prenderia o jogador nela para sempre.

O mapa inteiro mora no bloco `static` de `Area`, uma linha por área. Mudar uma
rota é mudar essa linha. O `AreaTest` trava as rotas uma a uma, então uma
mudança feita sem querer quebra o teste.

A área decide o **cenário**, a cor da interface e **quem aparece** ao explorar.

### Encontros por área

**O habitat sai do tipo.** A tabela de `Habitats` diz onde cada tipo vive:

| Tipo | Vive em |
|---|---|
| Planta  | Floresta, Floresta Profunda |
| Fogo    | Vulcão, e Floresta em menor número |
| Água    | Praia, Mar, Mar Profundo |
| Pedra   | Montanha, Ravina, Vulcão |
| Gelo    | Ravina Congelada |
| Sombrio | Floresta Profunda, Mar Profundo, Montanha Negra |
| Sagrado | Palácio Sagrado |

Uma espécie vive nas áreas em que **todos** os seus tipos vivem. Para as de
dois tipos isso dá o encontro dos dois: Sombrio / Água só no Mar Profundo,
Sombrio / Planta só na Floresta Profunda, Fogo / Pedra só no Vulcão. Uma espécie
nova, então, entra no mapa certo sozinha, só por ter o tipo que tem.

O sorteio é por peso (`Especies.pesoEm`): quem é comum na área pesa `PESO_COMUM`
(3), e quem tem um tipo raro lá pesa `PESO_RARO` (1). Na Floresta, o Folharal
aparece em 3 de cada 4 encontros e o Braseiro em 1.

| Área | Quem aparece |
|---|---|
| Floresta | Folharal, Braseiro (raro) |
| Floresta Profunda | Folharal, Penumbra, Tocumbra |
| Vulcão | Braseiro, Calhau, Brasalto |
| Montanha, Ravina | Calhau |
| Ravina Congelada | Nevisco |
| Montanha Negra | Penumbra |
| Palácio Sagrado | Candeio |
| Praia, Mar | Marulho |
| Mar Profundo | Marulho, Penumbra, Breumar |

Com as dez espécies de hoje, várias áreas têm uma espécie só. Cada espécie nova
de um tipo entra direto nas áreas desse tipo. O `EncontrosPorAreaTest` trava a
tabela e garante que nenhuma área fique vazia e nenhuma espécie fique sem lugar.

O tipo **predominante** de cada área (`Area.getTipoPredominante`) é outra coisa:
ele só decide que imagens soltas a área pega emprestadas como cenário (ver
[Cenários](#cenários)).

A área atual vai para o jogo salvo pelo **id** (`floresta-profunda`,
`palacio-sagrado`…), que, como o id das espécies, nunca deve mudar.

## Registro de espécies

Todas as espécies do jogo vivem em `Especies`, cada uma como uma ficha
`EspecieCriatura`. É de lá que saem os encontros selvagens, a tela de escolha
inicial e o bestiário — nenhum deles tem a sua própria lista de espécies.

Acrescentar uma espécie é acrescentar um bloco:

```java
public static final EspecieCriatura PEDRISCO = registrar(
        EspecieCriatura.novo("pedrisco", "Pedrisco")
                .tipo(TipoElemental.PEDRA)
                .base(60, 55, 70, 40, 50, 45).rendimentoExp(65)
                .grupoExp(GrupoExperiencia.LENTO)
                .aparencia(new Color(140, 120, 90), Adorno.NENHUM)
                .nomes("Pedrisco", "Rochedo")
                .aprende(1,  Golpes.INVESTIDA)
                .aprende(10, Golpes.PEDRADA)
                .construir());
```

Só isso: a espécie passa a aparecer nos encontros, no bestiário e no jogo salvo.
Marcá-la com `.inicial()` a coloca também na tela de escolha.

Para dois tipos, troque `.tipo(...)` por `.tipos(principal, secundario)`. O
principal dá a cor da espécie na interface. As áreas onde ela aparece saem dos
tipos ([Encontros por área](#encontros-por-área)). Para restringir ainda mais —
uma espécie de Fogo que só aparece no Vulcão, e não na Floresta —, acrescente
`.habitat(Area.VULCAO)`. O `habitat` só restringe: não põe Fogo no Mar.

Sem `.construtor(...)` a espécie usa `CriaturaComum`, que não tem habilidade
especial — **nenhuma classe nova precisa ser escrita**. Uma subclasse de
`Criatura` só é necessária quando a espécie tem habilidade própria, e nesse caso
ela carrega apenas o `multiplicadorHabilidade` e o `efeitoPosAtaque`: os números
continuam na ficha.

O **id** (`"pedrisco"`) é gravado no jogo salvo e no bestiário, e por isso nunca
deve mudar. O nome de exibição pode ser reescrito à vontade.

A aparência também mora na ficha, e não no tipo: duas espécies de Fogo podem ter
cores e adornos diferentes. Os adornos disponíveis hoje são `CHAMA`, `ONDAS`,
`FOLHAS`, `ESPINHOS`, `CRISTAL`, `CHIFRES`, `AUREOLA`, `ANTENA`, `GALHOS`, `MAGMA`
e `NENHUM` — cada espécie
atual usa um diferente.

## Cenários

O fundo da arena é o **cenário da área** onde o jogador está, tanto explorando
quanto em batalha. Cada batalha sorteia de novo entre as imagens da área.

Os cenários são apenas os arquivos de imagem de uma pasta — não existe lista
escrita a mão em lugar nenhum do código. A pasta procurada é
`imagens de referencia/` (ou `cenarios/`, ou `assets/cenarios/`), a partir do
diretório de onde o jogo foi executado.

### Uma subpasta por área

Acrescentar um cenário a uma área é copiar a imagem para a subpasta com o nome
dela:

```
imagens de referencia/
├── Floresta/
├── Floresta Profunda/
├── Vulcão/
├── Montanha/
├── Montanha Negra/
├── Palácio Sagrado/
├── Ravina/
├── Ravina Congelada/
├── Praia/
├── Mar/
└── Mar Profundo/
```

O nome do arquivo dentro da subpasta não importa: quem decide é a pasta. O nome
da pasta aceita acento ou não, maiúsculas ou não, e espaço, `_` ou `-`
(`Palácio Sagrado`, `palacio_sagrado` e `palacio-sagrado` dão na mesma área).
Subpastas com outros nomes são ignoradas.

### Imagens soltas

Uma área **sem imagem própria** pega emprestadas as imagens soltas na raiz da
pasta que forem do seu [tipo predominante](#mapa-e-rotas): as florestas usam as
florestas soltas, o Vulcão os vulcões, a Praia e os mares os oceanos, e o Palácio
Sagrado a `light.jpg`. O tipo da imagem solta **sai do nome do arquivo**, testado
nesta ordem:

| O nome contém | Vira cenário de |
|---|---|
| `volcano`, `vulcão`, `lava`, `magma`, `fire`, `fogo`, `ember`, `inferno` | **Fogo** |
| `ice`, `gelo`, `gelad…`, `snow`, `neve`, `frozen`, `congelad…`, `glacier`, `geleira`, `frost`, `geada`, `winter`, `inverno` | **Gelo** |
| `ocean`, `oceano`, `sea`, `mar`, `water`, `água`, `river`, `rio`, `lake`, `lago`, `beach`, `praia` | **Água** |
| `forest`, `floresta`, `bosque`, `jungle`, `selva`, `wood`, `mata`, `grove`, `garden`, `jardim` | **Planta** |
| `rock`, `stone`, `pedra`, `rocha`, `mountain`, `montanha`, `canyon`, `cliff`, `penhasco`, `ravina`, `cave`, `caverna` | **Pedra** |
| `dark`, `shadow`, `sombra`, `sombri…`, `night`, `noite`, `abyss`, `abismo`, `trevas` | **Sombrio** |
| `holy`, `sacred`, `sagrad…`, `temple`, `templo`, `cathedral`, `catedral`, `palace`, `palácio`, `shrine`, `light`, `luz` | **Sagrado** |
| qualquer outra coisa | **neutro** |

A ordem resolve os nomes que cairiam em dois tipos: `frozen lake` é Gelo, e
`dark forest` e `twilight forest` são Planta.

As imagens neutras soltas (`storm one.jpg`, por exemplo) não vão para área
nenhuma. Para usar uma delas, é só movê-la para a subpasta de uma área.

Acentos e maiúsculas não importam. O nome de exibição, mostrado discretamente no
canto da arena, também vem do arquivo: `twilight forest.jpg` → "Twilight Forest".

### Corte e contorno

Como a arena é uma faixa larga (perto de 2,7:1), a imagem é **redimensionada para
cobrir e cortada**, nunca esticada. A faixa que sobrevive ao corte depende do
tipo: cenários de Fogo cortam mais para baixo, onde costuma estar a lava; os de
floresta, um pouco acima do meio, para manter copa e chão juntos.

Os sprites ganharam um **contorno duplo** — um traço escuro por dentro e um claro
por fora — porque sobre uma foto o traço escuro sozinho sumiria nas partes
escuras da imagem, e o claro sumiria nas claras.

**Nenhuma imagem é obrigatória.** Sem imagem para a área, com a pasta vazia ou
com um arquivo ilegível, a arena pinta um gradiente na cor da área.

### Por que as imagens não estão no repositório

A pasta `imagens de referencia/` está no `.gitignore`. As imagens que estão nela
hoje são **arte de terceiros** — várias trazem a assinatura do artista visível —
e este repositório é público. Publicá-las aqui seria redistribuir trabalho alheio
sem licença, além de contradizer a promessa de arte original que abre este README.

Se você tiver os direitos das imagens (arte própria, licença comercial ou domínio
público), apague a linha `imagens de referencia/` do `.gitignore` e elas passam a
ser versionadas normalmente.

## Jogo salvo

Um save guarda a equipe (na ordem em que está), o bestiário e a área onde o
jogador está.

### Slots

Há **5 slots** e um de **salvamento rápido**, cada um num arquivo de
`<pasta do usuário>/.arenaelemental/`:

| Slot | Arquivo |
|---|---|
| Salvamento rápido | `rapido.save` |
| Slot 1 | `jogo.save` — o mesmo de quando o jogo tinha um save só; a partida antiga aparece aqui |
| Slots 2 a 5 | `jogo-2.save` … `jogo-5.save` |

- **Salvar / Sair**, na exploração, abre a lista de slots. Cada um mostra o
  líder da equipe, a área, a data e quantas espécies foram capturadas.
  **Salvar aqui** pergunta antes de gravar por cima de um save existente.
- **Salvar rápido**, na mesma lista, ou a tecla **F5** durante a exploração,
  grava direto no slot rápido, sem perguntar. No meio de uma batalha, não salva.
- **Sair do jogo**, no rodapé da lista, fecha o jogo. Com uma jornada em
  andamento, pergunta antes se quer salvar no slot de onde ela veio (ou onde foi
  salva por último); numa jornada que nunca foi salva, oferece o salvamento
  rápido. O **X** da janela faz a mesma pergunta.

No menu inicial, **Continuar Jornada** carrega o save gravado por último (o nome
do slot e a data aparecem embaixo do botão) e **Carregar Jogo** abre a lista para
escolher. Um arquivo que não pode ser lido aparece como "Arquivo ilegível" na
lista, sem impedir os outros de abrir.

A quantidade de slots é a constante `SlotDeSave.QUANTIDADE`.

### Formato

O arquivo é texto UTF-8, legível e editável à mão:

```
versao=3
salvoEm=2026-09-01T11:30:00
treinador=Treinador
area=floresta
criaturas=2
criatura.0.especie=braseiro
criatura.0.nome=Ignivo
criatura.0.nivel=12
criatura.0.exp=1750
criatura.0.vida=30
criatura.0.golpes=investida,brasa,presa-ignea
criatura.0.pp=investida:30,brasa:12,presa-ignea:15
criatura.0.condicao=QUEIMADURA
bestiario.braseiro=CAPTURADA
bestiario.marulho=VISTA
```

Duas decisões que fazem esse formato durar:

- **Nada de serialização binária.** Um `.ser` quebra assim que uma classe muda de
  campo, e este projeto vai mudar muito. O que se grava são os *ids* da espécie e
  dos golpes, estáveis por contrato — o save sobrevive a renomear classes,
  reequilibrar status ou trocar o nome de exibição de uma espécie.
- **Gravação atômica.** O jogo escreve num arquivo temporário ao lado e só então
  o move por cima do save. Um desligamento no meio da gravação deixa o save
  anterior intacto, em vez de um arquivo pela metade.

Ao carregar, o que o arquivo pedir e o jogo não souber reconstruir — uma espécie
ou um golpe que não existem mais — vira um **aviso** e o resto carrega
normalmente, em vez de o jogo se recusar a abrir. Nível, experiência e vida são
limitados às faixas válidas: um arquivo editado à mão consegue produzir uma
criatura *diferente*, nunca uma criatura inválida.

As chaves `pp`, `condicao` e `turnosDeSono` chegaram na **versão 2**, e `area` na
**versão 3**. Todas são opcionais. Um save da versão 1, gravado antes de PP e
condições existirem, continua abrindo: os golpes voltam com os PP cheios e sem
condição, que é exatamente o que aquele arquivo significava. Do mesmo jeito, um
save das versões 1 e 2, de antes do mapa, recomeça na Floresta. Uma área que não
existe mais vira aviso e também leva à Floresta.

## Status

Cada criatura tem os seis status dos jogos originais:

| Status | Papel |
|---|---|
| **HP**  | vida |
| **Atk** | ataque físico |
| **Def** | defesa física |
| **SpA** | ataque especial |
| **SpD** | defesa especial |
| **Spe** | velocidade — decide quem ataca primeiro no turno |

Os valores não são escritos à mão: saem dos status base da espécie combinados
com o nível, pelas fórmulas oficiais.

```
HP     = floor((2 × Base + IV + floor(EV/4)) × Nível / 100) + Nível + 10
Outros = floor((2 × Base + IV + floor(EV/4)) × Nível / 100) + 5
```

IVs, EVs e naturezas ainda **não são mecânicas do jogo**: o IV fica fixo em 31 e
o EV em 0 para todo mundo. Os termos já estão na fórmula para que essas
mecânicas entrem depois sem reescrevê-la.

Status base das espécies — BST 320 em todas, para nenhuma sair na frente. O que
muda é a distribuição:

| Espécie | Tipo | HP | Atk | Def | SpA | SpD | Spe | Perfil |
|---|---|---|---|---|---|---|---|---|
| Braseiro | Fogo    | 44 | 58 | 42 | 62 | 48 | 66 | rápido e frágil |
| Marulho  | Água    | 52 | 50 | 62 | 54 | 58 | 44 | defensivo |
| Folharal | Planta  | 48 | 46 | 52 | 64 | 56 | 54 | equilibrado |
| Calhau   | Pedra   | 56 | 62 | 72 | 36 | 50 | 44 | lento, físico e resistente |
| Nevisco  | Gelo    | 46 | 44 | 48 | 64 | 52 | 66 | rápido e especial |
| Penumbra | Sombrio | 48 | 64 | 44 | 56 | 44 | 64 | rápido e agressivo |
| Candeio  | Sagrado | 58 | 40 | 54 | 60 | 68 | 40 | lento, resistente no especial |
| Breumar  | Sombrio / Água   | 54 | 58 | 50 | 62 | 52 | 44 | atacante especial |
| Tocumbra | Sombrio / Planta | 60 | 62 | 60 | 44 | 54 | 40 | atacante físico, resistente |
| Brasalto | Fogo / Pedra     | 58 | 66 | 68 | 50 | 44 | 34 | lento, forte e resistente |

Só Braseiro, Marulho e Folharal são iniciais. Cada espécie aparece nos encontros
das áreas do seu tipo (ver [Encontros por área](#encontros-por-área)).

## Cálculo de dano

`CalculadoraDano` implementa a fórmula da 9ª geração, truncando os decimais
para baixo a cada etapa antes do multiplicador seguinte:

```
Dano = [ (((2 × Nível ÷ 5 + 2) × Poder × Ataque ÷ Defesa) ÷ 50) + 2 ]
       × Targets × Weather × Critical × Random × STAB × Type × Burn × Item × Screens × Other
```

| Termo | Situação no projeto |
|---|---|
| **Ataque / Defesa** | Atk contra Def em golpes físicos, SpA contra SpD em especiais |
| **Critical** | 1,5× com chance de 1/24 |
| **Random**   | inteiro sorteado de 85 a 100, dividido por 100 |
| **STAB**     | 1,5× quando o tipo do golpe é o da criatura |
| **Type**     | pela tabela de efetividade: 2× / 1× / 0,5×, e de 0,25× a 4× contra dois tipos |
| **Burn**     | 0,5× quando o atacante está queimado e o golpe é físico |
| **Other**    | carrega a habilidade da espécie |
| **Targets**  | sempre 1 — não há batalha em dupla |
| **Weather**  | sempre 1 — não há clima |
| **Item**     | sempre 1 — não há itens equipados |
| **Screens**  | sempre 1 — não há Reflect/Light Screen |

Terastalização nunca será implementada, então o STAB é sempre 1,5× e o termo que
ela acrescentaria não existe. Um golpe que acerta sempre tira ao menos 1 de vida.

Exemplo conferido à mão e travado em teste — Braseiro Nv.50 (SpA 82) usando
Labareda (Fogo, especial, poder 90) contra Folharal Nv.50 (SpD 76):

```
fatorNível = (2 × 50) ÷ 5 + 2            = 22
base       = 22 × 90 × 82 ÷ 76 ÷ 50 + 2  = 44
rolagem 100 → 44  →  STAB 1,5 → 66  →  tipo 2× → 132
rolagem  85 → 37  →  STAB 1,5 → 55  →  tipo 2× → 110
```

## Precisão e PP

Nem todo golpe acerta, e nenhum golpe é infinito.

- **Precisão** é a chance de acertar, de 0 a 100. Um golpe de precisão 100 nunca
  erra; abaixo disso o jogo sorteia. Errar **gasta o PP do mesmo jeito**, como
  nos jogos originais, e um golpe que erra não deixa efeito de status.
- **PP** são contados por criatura, não por golpe: dois Braseiros gastam os PP de
  Brasa separadamente. O botão do golpe mostra `PP atual/máximo` e fica
  desabilitado quando zera.

Os golpes mais fortes pagam por isso em precisão e em PP — é o que faz a escolha
do golpe importar, em vez de sempre valer a pena usar o mais forte.

### Esforço

Quando **todos** os golpes ficam sem PP, entra em cena o **Esforço**: um golpe de
último recurso que aparece sozinho na lista, nunca erra, não gasta PP e cobra um
quarto do HP máximo de quem o usa.

Ele não é enfeite: sem ele, uma criatura sem PP não teria nenhuma ação possível e
a batalha travaria — a mesma classe de problema que a falta de cura causava antes
do botão Descansar.

## Condições de status

Só uma condição não volátil por vez, como nos jogos originais. Cada uma age em um
momento diferente do turno:

| Condição | Antes de agir | No dano | Fim do turno |
|---|---|---|---|
| **Queimadura** (QUE) | — | golpes físicos a 0,5× (o termo *Burn*) | perde 1/16 do HP máximo |
| **Paralisia** (PAR)  | 25% de perder o turno | Velocidade pela metade | — |
| **Veneno** (VEN)     | — | — | perde 1/8 do HP máximo |
| **Sono** (SON)       | não age por 1 a 3 turnos | — | — |
| **Congelamento** (CON) | não age; 20% de descongelar por turno | — | — |

Detalhes que seguem os jogos originais:

- Uma criatura do tipo **Fogo não se queima**, e uma do tipo **Gelo não congela**
  — vale para qualquer um dos tipos de quem tem dois.
- Quem acorda no começo do turno **já age nesse mesmo turno**.
- A Velocidade cortada pela paralisia é a que decide a ordem do turno, então um
  paralisado pode perder a iniciativa que tinha.
- Uma criatura derrotada perde a condição; subir de nível **não** cura. Só o
  Descansar cura.
- O dano de fim de turno pode derrubar a criatura.

A condição aparece como uma etiqueta colorida ao lado da barra de vida (QUE, PAR,
VEN, SON, CON) e na tela de Equipe.

### Quais golpes causam o quê

| Golpe | Tipo | Categoria | Poder | Precisão | PP | Efeito |
|---|---|---|---|---|---|---|
| Investida        | Normal | Físico   | 40 | 100% | 35 | — |
| Trancada         | Normal | Físico   | 80 | 100% | 15 | 30% paralisia |
| Fumaça Tóxica    | Normal | Status   | —  |  90% | 10 | envenena |
| Esforço          | Normal | Físico   | 50 | 100% | ∞  | custa 25% do próprio HP |
| Brasa            | Fogo   | Especial | 40 | 100% | 25 | 10% queimadura |
| Presa Ígnea      | Fogo   | Físico   | 65 |  95% | 15 | 10% queimadura |
| Labareda         | Fogo   | Especial | 90 |  95% | 15 | 10% queimadura |
| Jato d'Água      | Água   | Especial | 40 | 100% | 25 | — |
| Aqua Garra       | Água   | Físico   | 60 | 100% | 20 | — |
| Sopro Gelado     | Água   | Especial | 55 |  95% | 15 | 10% congelamento |
| Maré Cheia       | Água   | Especial | 90 |  95% | 15 | — |
| Chicote de Vinha | Planta | Físico   | 45 | 100% | 25 | — |
| Folha Navalha    | Planta | Especial | 55 |  95% | 25 | — |
| Esporo Sonífero  | Planta | Status   | —  |  75% | 15 | faz dormir |
| Bomba Semente    | Planta | Físico   | 80 | 100% | 15 | — |
| Pedrada          | Pedra  | Físico   | 40 | 100% | 25 | — |
| Rocha Lançada    | Pedra  | Físico   | 65 |  95% | 15 | — |
| Tremor           | Pedra  | Físico   | 55 | 100% | 15 | 20% paralisia |
| Desmoronamento   | Pedra  | Físico   | 90 |  90% | 10 | — |
| Granizo          | Gelo   | Especial | 40 | 100% | 25 | — |
| Rajada Gélida    | Gelo   | Especial | 65 |  95% | 15 | — |
| Geada            | Gelo   | Especial | 50 |  95% | 15 | 15% congelamento |
| Nevasca          | Gelo   | Especial | 90 |  90% | 10 | 10% congelamento |
| Garra Sombria    | Sombrio | Físico  | 40 | 100% | 25 | — |
| Golpe Traiçoeiro | Sombrio | Físico  | 65 |  95% | 15 | — |
| Pesadelo         | Sombrio | Status  | —  |  75% | 15 | faz dormir |
| Eclipse          | Sombrio | Especial | 90 |  95% | 15 | — |
| Lampejo          | Sagrado | Especial | 40 | 100% | 25 | — |
| Lança de Luz     | Sagrado | Especial | 60 | 100% | 20 | — |
| Clarão Ofuscante | Sagrado | Status  | —  |  90% | 15 | paralisa |
| Luz Divina       | Sagrado | Especial | 90 |  95% | 15 | — |

O Sopro Gelado continua sendo de **Água**: ele é o golpe de congelamento do
Marulho, e mudá-lo para Gelo tiraria dele o STAB do Marulho.

## Troca de criatura

O botão **Trocar**, na coluna ao lado dos golpes, abre as casas da equipe, com
cada criatura mostrando nível, vida e condição. Trocar durante a batalha **gasta
o turno**: a selvagem ataca de graça, e quem leva o golpe é quem entrou.

Quando a criatura em campo cai e ainda há reserva de pé, a lista abre sozinha e
**sem o botão de cancelar** — a batalha não pode continuar sem alguém em campo.
Essa substituição não gasta turno: a queda já custou o dela. Só quando a equipe
inteira está no chão é que a batalha termina em derrota.

Fora da batalha, quem lidera a equipe se escolhe na tela de
[Equipe](#equipe).

A experiência vai para quem estiver em campo quando a selvagem cair. Os jogos
originais dividem entre todos os participantes; aqui o número de participantes
ainda é sempre 1 na fórmula.

## Aleatoriedade

Todo sorteio do jogo sai de um `java.util.Random` recebido por construtor, nunca
de `Math.random()`. `Batalha` cria o seu e o compartilha com `CalculadoraDano` e
`Captura`, de modo que **uma semente fixa reproduz a batalha inteira** — dano,
ordem do turno, precisão, efeitos de status, dano residual, golpe da selvagem e
resultado da captura. `FabricaCriaturas` funciona igual: com a mesma semente, a
mesma sequência de encontros.

Os atalhos estáticos (`FabricaCriaturas.selvagemAleatorio`, o construtor de
`Batalha` sem `Random`) continuam existindo para quem não se importa com a
semente. Quem se importa — os testes — passa a sua.

## Tipos

Sete tipos de criatura. Os cinco da natureza formam um **ciclo equilibrado**:
cada um é supereficaz contra dois, pouco eficaz contra outros dois, e todo par
tem um vencedor. O triângulo clássico Fogo > Planta > Água > Fogo continua dentro
dele. Sombrio e Sagrado ficam **fora do ciclo**: são supereficazes um contra o
outro e neutros contra todo o resto.

| atacante ↓ / defensor → | Fogo | Água | Planta | Pedra | Gelo | Sombrio | Sagrado |
|---|---|---|---|---|---|---|---|
| **Fogo**    | 1×   | 0,5× | 2×   | 0,5× | 2×   | 1× | 1× |
| **Água**    | 2×   | 1×   | 0,5× | 2×   | 0,5× | 1× | 1× |
| **Planta**  | 0,5× | 2×   | 1×   | 2×   | 0,5× | 1× | 1× |
| **Pedra**   | 2×   | 0,5× | 0,5× | 1×   | 2×   | 1× | 1× |
| **Gelo**    | 0,5× | 2×   | 2×   | 0,5× | 1×   | 1× | 1× |
| **Sombrio** | 1×   | 1×   | 1×   | 1×   | 1×   | 1× | 2× |
| **Sagrado** | 1×   | 1×   | 1×   | 1×   | 1×   | 2× | 1× |

Resumo por tipo:

| Tipo | Supereficaz contra | Fraco contra |
|---|---|---|
| Fogo    | Planta, Gelo | Água, Pedra |
| Água    | Fogo, Pedra  | Planta, Gelo |
| Planta  | Água, Pedra  | Fogo, Gelo |
| Pedra   | Fogo, Gelo   | Água, Planta |
| Gelo    | Planta, Água | Fogo, Pedra |
| Sombrio | Sagrado      | — |
| Sagrado | Sombrio      | — |

Um tipo contra ele mesmo é sempre 1×, e não há imunidades (multiplicador 0).

### Dois tipos

Uma espécie pode ter um ou dois tipos. Com dois:

- **Na defesa, os multiplicadores se multiplicam.** O Brasalto (Fogo / Pedra)
  leva **4×** de Água (2× no Fogo e 2× na Pedra), **0,25×** de Gelo, e **1×** de
  Planta, porque o 0,5× no Fogo e o 2× na Pedra se anulam.
- **No ataque, o STAB vale para os dois tipos.** O Breumar (Sombrio / Água)
  ganha 1,5× tanto na Garra Sombria quanto no Jato d'Água.
- **As imunidades de condição valem para qualquer um dos dois**: o Brasalto não
  se queima.

O **tipo principal** (o primeiro) é o que dá a cor da espécie nos botões e no
cartão de vida; o cartão mostra os dois tipos e divide o filete lateral nas duas
cores.

| Espécie | Tipos | 4× | 2× | 0,5× | 0,25× |
|---|---|---|---|---|---|
| Breumar  | Sombrio / Água   | — | Sagrado, Planta, Gelo | Fogo, Pedra | — |
| Tocumbra | Sombrio / Planta | — | Sagrado, Fogo, Gelo   | Água, Pedra | — |
| Brasalto | Fogo / Pedra     | Água | Pedra | Fogo | Gelo |
O `TipoElementalTest` trava a tabela e as propriedades do ciclo: se alguém mexer
numa linha e desequilibrar os cinco, o teste avisa.

A tabela mora num bloco `static` de `TipoElemental`, uma linha `forteContra` e
uma `fracoContra` por tipo.

Existe também o tipo **Normal**, usado só por golpes: causa dano 1× contra todos
e nunca recebe STAB, porque nenhuma criatura é desse tipo.

## Golpes e aprendizado

Toda criatura carrega no máximo 4 golpes, aprendidos por nível — com o repertório
cheio, o golpe novo entra no lugar do mais antigo.

| Nível | Braseiro | Marulho | Folharal |
|---|---|---|---|
| 1  | Investida        | Investida        | Investida        |
| 5  | Brasa            | Jato d'Água      | Chicote de Vinha |
| 15 | Presa Ígnea      | Aqua Garra       | Folha Navalha    |
| 20 | Fumaça Tóxica    | Sopro Gelado     | Esporo Sonífero  |
| 25 | Trancada         | Trancada         | Trancada         |
| 35 | Labareda         | Maré Cheia       | Bomba Semente    |

| Nível | Calhau | Nevisco | Penumbra | Candeio |
|---|---|---|---|---|
| 1  | Investida      | Investida     | Investida        | Investida        |
| 5  | Pedrada        | Granizo       | Garra Sombria    | Lampejo          |
| 15 | Rocha Lançada  | Rajada Gélida | Golpe Traiçoeiro | Lança de Luz     |
| 20 | Tremor         | Geada         | Pesadelo         | Clarão Ofuscante |
| 25 | Trancada       | Trancada      | Trancada         | Trancada         |
| 35 | Desmoronamento | Nevasca       | Eclipse          | Luz Divina       |

O golpe do nível 20 é o que dá a cada espécie acesso a uma condição de status
própria: veneno para o Braseiro, congelamento para o Marulho e o Nevisco, sono
para o Folharal e a Penumbra, e paralisia para o Calhau e o Candeio. As três de
dois tipos misturam os golpes dos seus dois tipos e aprendem mais devagar — o
golpe mais forte só chega no nível 38:

| Nível | Breumar | Tocumbra | Brasalto |
|---|---|---|---|
| 1  | Investida     | Investida        | Investida      |
| 5  | Garra Sombria | Garra Sombria    | Brasa          |
| 10 | Jato d'Água   | Chicote de Vinha | Pedrada        |
| 20 | Pesadelo      | Esporo Sonífero  | Tremor         |
| 30 | Maré Cheia    | Golpe Traiçoeiro | Presa Ígnea    |
| 38 | Eclipse       | Bomba Semente    | Desmoronamento |

Queimadura e
paralisia também vêm dos golpes de dano.

## Nível e experiência

Nível vai de 1 a 100. Todas as espécies usam a curva **Médio Rápido** (n³), então
o nível *n* exige n³ de experiência acumulada — 1.000.000 no nível 100.

A experiência ganha usa a fórmula escalada por nível da 5ª geração em diante:

```
Exp = floor( (b × L ÷ 5) × (1 ÷ s) × ((2L + 10)^2.5 ÷ (L + Lp + 10)^2.5) ) + 1
```

com **b** = rendimento da espécie derrotada (Braseiro 62, Marulho 63,
Folharal 64, Calhau 64, Nevisco 63, Penumbra 63, Candeio 65, e 66 nas três de
dois tipos), **L** = nível dela, **Lp** = nível de quem venceu e **s** = número
de participantes (sempre 1). Derrotar alguém bem acima do seu nível rende muito
mais: um Braseiro Nv.5 ganha 65 de experiência vencendo um Folharal Nv.5, e
2.385 vencendo um Nv.50.

Ao subir de nível os status são recalculados e o HP máximo cresce — a vida atual
sobe junto, na mesma quantidade. A barra de EXP fica visível abaixo da barra de
vida durante a batalha.

## Habilidades especiais (polimorfismo)

Cada subclasse de `Criatura` pode sobrescrever `multiplicadorHabilidade(...)`,
que entra na fórmula como o termo **Other**, e `efeitoPosAtaque(...)`:

- **Braseiro** (Fogo): +30% de dano contra alvos com vida abaixo de 30%
- **Marulho** (Água): +15% de dano enquanto a própria vida estiver acima de 50%
- **Folharal** (Planta): recupera 20% do dano causado como vida a cada ataque
- **Calhau** (Pedra) — *Rolo Compressor*: +25% de dano contra alvos mais rápidos que ele
- **Nevisco** (Gelo) — *Frio Cortante*: +25% de dano contra alvos com condição de status
- **Penumbra** (Sombrio) — *Emboscada*: +30% de dano contra alvos com a vida cheia
- **Candeio** (Sagrado) — *Bênção*: recupera 1/16 do HP máximo a cada golpe que causa dano
- **Breumar** (Sombrio / Água) — *Isca Luminosa*: +25% de dano com golpes especiais
- **Tocumbra** (Sombrio / Planta) — *Emaranhado*: +25% de dano com golpes físicos
- **Brasalto** (Fogo / Pedra) — *Erupção*: +30% de dano enquanto a própria vida estiver abaixo da metade

## Bestiário

O bestiário guarda três estados por espécie: **não vista**, **vista** e
**capturada**. Encontrar uma criatura selvagem marca a espécie como vista — vale
mesmo que você fuja; capturá-la sobe para capturada, e esse estado nunca regride.

Na tela de Equipe, o bestiário mostra **todas** as espécies registradas: as
conhecidas com nome, tipo e a habilidade no tooltip; as ainda não encontradas
como silhueta cinza com "???".

As entradas são guardadas pelo **id da espécie**, não pelo nome da classe Java.
Renomear ou trocar a classe de uma espécie não apaga o progresso de quem já
jogou, e espécies sem subclasse própria contam normalmente.

## Cura

O botão **Descansar**, na tela de exploração, restaura a equipe inteira —
inclusive as criaturas desmaiadas — devolvendo **vida, PP e condições de
status**. É o Centro de Cura em forma de botão: sem custo, sem limite de uso e
sem tela própria. Serve como base para itens de cura ou um centro de verdade mais
adiante.

## O que ainda não existe

Deixado de fora de propósito, para entrar em etapas futuras:

- itens equipados (*Held Items*) e itens de cura (poções)
- clima, *screens* e batalhas em dupla
- IVs, EVs e naturezas
- alterações de status em batalha (aumentar Ataque, baixar Defesa…)
- condições voláteis (confusão, encanto…) — as não voláteis já existem
- experiência dividida entre vários participantes
- Terastalização — decisão de projeto, não entra nunca
