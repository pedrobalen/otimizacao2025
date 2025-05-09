const fs = require('fs');

function calcularIntersecao(x1_r1, y1_r1, x2_r1, y2_r1, x1_r2, y1_r2, x2_r2, y2_r2) {
    let a1, b1;
    if (x1_r1 === x2_r1) {
        a1 = Infinity;
        b1 = x1_r1; 
    } else {
        a1 = (y2_r1 - y1_r1) / (x2_r1 - x1_r1);
        b1 = y1_r1 - a1 * x1_r1;
    }
    
    let a2, b2;
    if (x1_r2 === x2_r2) {
        a2 = Infinity;
        b2 = x1_r2; 
    } else {
        a2 = (y2_r2 - y1_r2) / (x2_r2 - x1_r2);
        b2 = y1_r2 - a2 * x1_r2;
    }
    
    if (a1 === a2) {
        if (b1 === b2) {
            return "Retas coincidentes (infinitos pontos de intersecao)";
        } else {
            return "Retas paralelas (sem intersecao)";
        }
    } else if (a1 === Infinity) {
        const x = b1;
        const y = a2 * x + b2;
        return `Ponto (${x}, ${y})`;
    } else if (a2 === Infinity) {
        const x = b2;
        const y = a1 * x + b1;
        return `Ponto (${x}, ${y})`;
    } else {
        const x = (b2 - b1) / (a1 - a2);
        const y = a1 * x + b1;
        return `Ponto (${x}, ${y})`;
    }
}

function processarArquivoDePontos(caminhoArquivo) {
    try {
        const conteudo = fs.readFileSync(caminhoArquivo, 'utf8');
        const linhas = conteudo.split('\n').filter(linha => linha.trim() !== '');
        const retas = [];
        for (let i = 0; i < linhas.length; i++) {
            const pontos = linhas[i].split('),(').map(p => p.replace(/[()]/g, ''));    
            if (pontos.length === 2) {
                const coord1 = pontos[0].split(',').map(coord => parseFloat(coord.trim()));
                const coord2 = pontos[1].split(',').map(coord => parseFloat(coord.trim()));
                if (coord1.length === 2 && coord2.length === 2 && 
                    !isNaN(coord1[0]) && !isNaN(coord1[1]) && 
                    !isNaN(coord2[0]) && !isNaN(coord2[1])) {
                    retas.push({
                        x1: coord1[0],
                        y1: coord1[1],
                        x2: coord2[0],
                        y2: coord2[1]
                    });
                    
                } else {
                    console.error(`Erro: formato invalido na linha ${i+1}`);
                }
            } else {
                console.error(`Erro: linha ${i+1} não contém dois pontos no formato (x1,y1),(x2,y2)`);
            }
        }
        
        console.log("\nResultados das interseções:");
        for (let i = 0; i < retas.length; i++) {
            for (let j = i + 1; j < retas.length; j++) {
                const reta1 = retas[i];
                const reta2 = retas[j];
                
                const resultado = calcularIntersecao(
                    reta1.x1, reta1.y1, reta1.x2, reta1.y2,
                    reta2.x1, reta2.y1, reta2.x2, reta2.y2
                );
                
                console.log(`Interseção entre Reta ${String.fromCharCode(65 + i)} e Reta ${String.fromCharCode(65 + j)}: ${resultado}`);
            }
        }
        
        return retas;
    } catch (erro) {
        console.error("Erro ao processar o arquivo:", erro.message);
        return [];
    }
}

function calcularEquacoesDasRetas(retas) {
    console.log("\nEquações das retas:");
    retas.forEach((reta, index) => {
        const { x1, y1, x2, y2 } = reta;
        let a, b;
        if (x1 === x2) {
            console.log(`Reta ${String.fromCharCode(65 + index)}: x = ${x1} (reta vertical)`);
        } else {
            a = (y2 - y1) / (x2 - x1);
            b = y1 - a * x1;
            console.log(`Reta ${String.fromCharCode(65 + index)}: y = ${a}x + ${b}`);
        }
    });
}

function verificaPrimeiroQuadranteDasIntersecoes(retas) {
    console.log("\nInterseções no primeiro quadrante:");
    retas.forEach((reta1, i) => {
        retas.slice(i + 1).forEach((reta2, j) => {
            const resultado = calcularIntersecao(
                reta1.x1, reta1.y1, reta1.x2, reta1.y2,
                reta2.x1, reta2.y1, reta2.x2, reta2.y2
            );

            if (resultado.includes("Ponto")) {
                const [x, y] = resultado.match(/-?\d+(\.\d+)?/g).map(Number);
                if (x > 0 && y > 0) {
                    console.log(`Interseção entre Reta ${String.fromCharCode(65 + i)} e Reta ${String.fromCharCode(65 + i + j + 1)} no primeiro quadrante: (${x}, ${y})`);
                }
            }
        });
    });
}

function encontrarMaiorValorXY(retas) {
    let maiorX = -Infinity;
    let maiorY = -Infinity;

    retas.forEach(reta => {
        const { x1, y1, x2, y2 } = reta;
        maiorX = Math.max(maiorX, x1, x2);
        maiorY = Math.max(maiorY, y1, y2);
    });

    console.log(`\nMaior valor de X: ${maiorX}`);
    console.log(`Maior valor de Y: ${maiorY}`);
    return { maiorX, maiorY };
}






function main() {
    const caminhoArquivo = process.argv[2];
    
    if (!caminhoArquivo) {
        console.error("Passe o caminho como argumento");
        return;
    }
    const retas = processarArquivoDePontos(caminhoArquivo);
    //processarArquivoDePontos(caminhoArquivo);
    calcularEquacoesDasRetas(retas);
    verificaPrimeiroQuadranteDasIntersecoes(retas);
    encontrarMaiorValorXY(retas)
}
main();
