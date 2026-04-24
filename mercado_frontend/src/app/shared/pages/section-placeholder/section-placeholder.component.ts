import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

interface SectionData {
  title: string;
  description: string;
  endpoint: string;
}

@Component({
  selector: 'app-section-placeholder',
  standalone: true,
  templateUrl: './section-placeholder.component.html',
  styleUrl: './section-placeholder.component.css'
})
export class SectionPlaceholderComponent {
  private readonly route = inject(ActivatedRoute);

  get data(): SectionData {
    return this.route.snapshot.data as SectionData;
  }
}
