import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Catalogo } from './pages/catalogo/catalogo';
import { Login } from './pages/login/login';
import { Registro } from './pages/registro/registro';
import { MiCuenta } from './pages/mi-cuenta/mi-cuenta';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'catalogo', component: Catalogo },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'mi-cuenta', component: MiCuenta },
  { path: '**', redirectTo: '' }
];