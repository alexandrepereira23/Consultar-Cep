export interface EnderecoApiResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  unidade: string;
  bairro: string;
  cidade: string;
  uf: string;
  estado: string;
  regiao: string;
  codigoIbge: string;
  ddd: string;
  siafi: string;
  gia: string;
  localizacao: {
    latitude: number;
    longitude: number;
  } | null;
  fonte: string;
}
