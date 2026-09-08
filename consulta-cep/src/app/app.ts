import { ChangeDetectionStrategy, Component } from '@angular/core';

import { ConsultaCepComponent } from './features/consulta-cep/consulta-cep';

@Component({
  selector: 'app-root',
  imports: [ConsultaCepComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
}
